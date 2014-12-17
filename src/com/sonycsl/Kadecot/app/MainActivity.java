/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.app;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.sonycsl.Kadecot.content.WifiConnectionBroadcastReceiver;
import com.sonycsl.Kadecot.core.R;
import com.sonycsl.Kadecot.net.ConnectivityManagerUtil;
import com.sonycsl.Kadecot.net.NetworkID;
import com.sonycsl.Kadecot.preference.EulaPreference;
import com.sonycsl.Kadecot.preference.KadecotServicePreference;
import com.sonycsl.Kadecot.preference.WebSocketServerPreference;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.service.KadecotService;
import com.sonycsl.Kadecot.service.ServerManager;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    LocalPagerAdapter mLocalPagerAdapter;
    private Intent mIntent;
    private ViewPager mViewPager;
    private int mCurrentItem = 0;
    private Handler mHandler;

    private WifiConnectionBroadcastReceiver mReceiver;
    private boolean mIsReceiverRegistered = false;

    private WifiDialogFragment mWifiDialogFragment;

    private OnSharedPreferenceChangeListener mListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(
                SharedPreferences sharedPreferences, String key) {
            if (!key.equals(EulaActivity.EULA_LABEL)) {
                return;
            }
            if (EulaPreference.isAgreed(MainActivity.this)) {
                registerReceiver(mReceiver, new IntentFilter(
                        ConnectivityManager.CONNECTIVITY_ACTION));
                mIsReceiverRegistered = true;
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener);
            }
        }
    };

    private void wifiSetUp(final String ssid, final String bssid) {

        WebSocketServerPreference.setEnabled(MainActivity.this, false);

        ContentProviderClient client = getContentResolver().acquireContentProviderClient(
                KadecotCoreStore.AccessPoints.CONTENT_URI);
        try {
            Cursor cursor = client.query(KadecotCoreStore.AccessPoints.CONTENT_URI, null,
                    KadecotCoreStore.AccessPoints.AccessPointColumns.SSID + "=? AND " +
                            KadecotCoreStore.AccessPoints.AccessPointColumns.BSSID + "=?",
                    new String[] {
                            ssid, bssid
                    }, null);
            int count = cursor.getCount();
            cursor.close();
            if (count > 0) {
                WebSocketServerPreference.setEnabled(this, true);
                Toast.makeText(MainActivity.this, R.string.message_connected, Toast.LENGTH_LONG)
                        .show();
                return;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            finish();
            return;
        } finally {
            client.release();
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (mWifiDialogFragment != null) {
            ft.remove(mWifiDialogFragment);
        }
        mWifiDialogFragment = WifiDialogFragment.newInstance(ssid, bssid);
        ft.add(mWifiDialogFragment, WifiDialogFragment.class.getSimpleName());
        ft.commitAllowingStateLoss();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        mReceiver = new WifiConnectionBroadcastReceiver(MainActivity.this) {

            private String atSSID(String ssid) {
                return " @ " + ssid.replace("\"", "");
            }

            @Override
            public void onConnected() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        NetworkID networkID = ConnectivityManagerUtil
                                .getCurrentNetworkID(MainActivity.this);

                        if (networkID == null) {
                            mHandler.postDelayed(this, 500);
                            return;
                        }
                        wifiSetUp(networkID.getSsid(), networkID.getBssid());
                        getActionBar().setTitle(getTitle());
                        getActionBar().setSubtitle(
                                ConnectivityManagerUtil.getIPAddress(MainActivity.this) + ":"
                                        + ServerManager.WS_PORT_NO + atSSID(networkID.getSsid()));
                    }
                });
            }

            @Override
            public void onDisconnected() {
                mHandler.removeCallbacksAndMessages(null);
                WebSocketServerPreference.setEnabled(MainActivity.this, false);
                getActionBar().setTitle(getTitle());
                getActionBar().setSubtitle(R.string.action_offline);
            }
        };
        mLocalPagerAdapter = new LocalPagerAdapter(getFragmentManager());

        final ActionBar actionBar = getActionBar();

        actionBar.setHomeButtonEnabled(false);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mLocalPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab().setText(mLocalPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }

        if (EulaPreference.isAgreed(this)) {
            registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            mIsReceiverRegistered = true;
        } else {
            SharedPreferences sp = getSharedPreferences(getString(R.string.preferences_file_name),
                    Context.MODE_PRIVATE);
            sp.registerOnSharedPreferenceChangeListener(mListener);
        }
        mIntent = new Intent(this, KadecotService.class);
        startService(mIntent);
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);

        if (mIsReceiverRegistered) {
            unregisterReceiver(mReceiver);
        }

        if (!KadecotServicePreference.isPersistentModeEnabled(this)) {
            WebSocketServerPreference.setEnabled(this, false);
            stopService(mIntent);
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewPager.setAdapter(mLocalPagerAdapter);
        mViewPager.setCurrentItem(mCurrentItem);
    }

    @Override
    protected void onPause() {
        mCurrentItem = mViewPager.getCurrentItem();
        super.onPause();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void finish() {
        if (!KadecotServicePreference.isPersistentModeEnabled(this)) {
            byebye();
            return;
        }

        ByebyeDialogFragment byebye = ByebyeDialogFragment.newInstance();
        byebye.show(getFragmentManager(), ByebyeDialogFragment.class.getSimpleName());
    }

    private void byebye() {
        super.finish();
    }

    public static final class WifiDialogFragment extends DialogFragment {

        private static final String SSID_KEY = "ssid";
        private static final String BSSID_KEY = "bssid";

        public static WifiDialogFragment newInstance(String ssid, String bssid) {
            WifiDialogFragment fragment = new WifiDialogFragment();
            Bundle args = new Bundle();
            args.putString(SSID_KEY, ssid);
            args.putString(BSSID_KEY, bssid);
            fragment.setArguments(args);
            return fragment;
        }

        public WifiDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);

            final String ssid = getArguments().getString(SSID_KEY);
            final String bssid = getArguments().getString(BSSID_KEY);

            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title_attention)
                    .setMessage(getString(R.string.message_connect_kadecot, ssid))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ContentProviderClient client = getActivity().getContentResolver()
                                    .acquireContentProviderClient(
                                            KadecotCoreStore.AccessPoints.CONTENT_URI);
                            ContentValues values = new ContentValues();
                            values.put(KadecotCoreStore.AccessPoints.AccessPointColumns.SSID, ssid);
                            values.put(KadecotCoreStore.AccessPoints.AccessPointColumns.BSSID,
                                    bssid);
                            try {
                                client.insert(KadecotCoreStore.AccessPoints.CONTENT_URI, values);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                                getActivity().finish();
                            } finally {
                                client.release();
                            }
                            WebSocketServerPreference.setEnabled(getActivity(), true);
                            Toast.makeText(getActivity(), R.string.message_connected,
                                    Toast.LENGTH_LONG).show();
                        }
                    }).create();
            return dialog;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            getActivity().finish();
        }

    }

    public static final class ByebyeDialogFragment extends DialogFragment {

        public static ByebyeDialogFragment newInstance() {
            return new ByebyeDialogFragment();
        }

        public ByebyeDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);
            Dialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title_background_available)
                    .setMessage(R.string.background_confirmation)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((MainActivity) getActivity()).byebye();
                        }
                    })
                    .setNegativeButton(R.string.stop_server, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            KadecotServicePreference.setPersistentModeEnabled(getActivity(), false);
                            ((MainActivity) getActivity()).byebye();
                        }
                    }).create();
            return dialog;
        }
    }

    private static class LocalPagerAdapter extends FragmentStatePagerAdapter {

        public LocalPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return DeviceListFragment.newInstance(0, 1);
                case 1:
                    return AppListFragment.newInstance(2);
                case 2:
                    return SettingsFragment.newInstance(R.xml.preferences);
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Devices";
                case 1:
                    return "Apps";
                case 2:
                    return "Settings";
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}
