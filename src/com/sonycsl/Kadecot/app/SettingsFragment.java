/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.app;

import android.app.AlertDialog;
import android.content.ContentProviderClient;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.sonycsl.Kadecot.core.R;
import com.sonycsl.Kadecot.net.ConnectivityManagerUtil;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.service.ServerManager;

public class SettingsFragment extends PreferenceFragment {

    private static final String PREFERENCE_RES_ID_KEY = "preference";

    public static SettingsFragment newInstance(int preferencesResId) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putInt(PREFERENCE_RES_ID_KEY, preferencesResId);
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(getString(R.string.preferences_file_name));
        addPreferencesFromResource(getArguments().getInt(PREFERENCE_RES_ID_KEY));

        PreferenceScreen ps = (PreferenceScreen) findPreference(getString(R.string.delete_all_inactive_devices_preference_key));
        if (ps != null) {
            ps.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.title_delete_all_inactive_devices)
                            .setMessage(R.string.message_delete_all_inactive_devices)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteInactiveDevice();
                                        }
                                    })
                            .setNegativeButton(android.R.string.no,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).create().show();
                    return true;
                }
            });
        }

        ps = (PreferenceScreen) findPreference(getString(R.string.access_point_list_preference_key));
        if (ps != null) {
            ps.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), AccessPointListActivity.class));
                    return true;
                }
            });

            ps = (PreferenceScreen) findPreference(getString(R.string.origin_list_preference_key));
            ps.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), OriginListActivity.class));
                    return true;
                }
            });
        }

        ps = (PreferenceScreen) findPreference(getString(R.string.plugin_list_preference_key));
        if (ps != null) {
            ps.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), PluginListActivity.class));
                    return true;
                }
            });
        }

        ps = (PreferenceScreen) findPreference(getString(R.string.about_kadecot_key));
        if (ps != null) {
            ps.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), EulaActivity.class));
                    return true;
                }
            });
        }

        ps = (PreferenceScreen) findPreference(getString(R.string.open_source_licenses_key));
        if (ps != null) {
            ps.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), LicensesActivity.class));
                    return true;
                }
            });
        }

        CheckBoxPreference cp = (CheckBoxPreference) findPreference(getString(R.string.jsonp_preference_key));
        if (cp != null) {
            cp.setSummaryOn("http://" + ConnectivityManagerUtil.getIPAddress(getActivity()) + ":"
                    + ServerManager.JSONP_PORT_NO + "/jsonp/v1/devices");
        }

        cp = (CheckBoxPreference) findPreference(getString(R.string.developer_mode_preference_key));
        if (cp != null) {
            cp.setSummaryOn("http://" + ConnectivityManagerUtil.getIPAddress(getActivity()) + ":"
                    + ServerManager.JSONP_PORT_NO + "/jsonp/v1/devices");
        }
    }

    private void deleteInactiveDevice() {
        ContentProviderClient client = getActivity().getContentResolver()
                .acquireContentProviderClient(
                        KadecotCoreStore.Devices.CONTENT_URI);
        try {
            client.delete(KadecotCoreStore.Devices.CONTENT_URI,
                    KadecotCoreStore.Devices.DeviceColumns.STATUS + " =?",
                    new String[] {
                        String.valueOf(0)
                    });
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            client.release();
        }
    }
}
