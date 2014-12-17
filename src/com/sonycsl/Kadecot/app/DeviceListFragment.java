/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.sonycsl.Kadecot.content.JsonLoader;
import com.sonycsl.Kadecot.core.R;
import com.sonycsl.Kadecot.net.ConnectivityManagerUtil;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.service.KadecotService;
import com.sonycsl.Kadecot.service.MessageReceiver;
import com.sonycsl.Kadecot.service.ResponseHandler;
import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.util.PairKeyMap;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DeviceListFragment extends ListFragment implements MessageReceiver {

    private static final String CURSOR_LOADER_ID_KEY = "cursor";
    private static final String JSON_LOADER_ID_KEY = "json";

    private LayoutInflater mInflater;
    private AndroidHttpClient mHttpClient;

    private int mClientId;
    private Messenger mMessenger;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMessenger = new Messenger(service);
            queryClientId();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            disposeClientId();
            mMessenger = null;
        }

        private void queryClientId() {
            int requestId = WampRequestIdGenerator.getId();
            Message msg = Message.obtain(null, KadecotService.MSGR_INTERFACE_VERSION, 0, requestId);
            msg.getData().putBoolean(KadecotService.MSGR_KEY_CONNECT, true);
            msg.replyTo = new Messenger(new ResponseHandler(DeviceListFragment.this));
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                mMessenger = null;
            }
        }

        private void disposeClientId() {
            mClientId = -1;
        }
    };

    private final Handler mHandler = new Handler();

    private void onChange(boolean selfChange) {
        if (isAdded()) {
            Loader<Cursor> loader = getLoaderManager().getLoader(getCursorLoaderId());
            if (loader != null) {
                loader.onContentChanged();
            }
        }
    }

    private final ContentObserver mDeviceObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            DeviceListFragment.this.onChange(selfChange);
        }
    };

    private final ContentObserver mDeviceTypeObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            DeviceListFragment.this.onChange(selfChange);
        }
    };

    private final LoaderCallbacks<Cursor> mCursorLoaderCallbacks = new LoaderCallbacks<Cursor>() {

        private DeviceCursorAdapter mAdapter;

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), KadecotCoreStore.Devices.CONTENT_URI, null,
                    null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if (mAdapter == null) {
                mAdapter = new DeviceCursorAdapter(getActivity(), cursor, 0, mInflater);
            } else {
                Cursor old = mAdapter.swapCursor(cursor);
                if (old != null) {
                    old.close();
                }
            }
            setListAdapter(mAdapter);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            DeviceCursorAdapter adapter = (DeviceCursorAdapter) getListAdapter();
            Cursor old = adapter.swapCursor(null);
            if (old != null) {
                old.close();
            }
        }
    };

    private final PairKeyMap<String, String, String> mRemoUrls = new PairKeyMap<String, String, String>();

    private final LoaderCallbacks<JSONObject> mJsonLoaderCallbacks = new LoaderCallbacks<JSONObject>() {

        @Override
        public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
            JsonLoader loader = new JsonLoader(getActivity(), mHttpClient,
                    getString(R.string.remos_json_url));
            loader.forceLoad();
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<JSONObject> loader, JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }

            JSONArray jsonArray;
            try {
                jsonArray = jsonObject.getJSONArray("data");
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                try {
                    JSONObject inout = jsonArray.getJSONObject(i);
                    JSONObject in = inout.getJSONObject("in");
                    JSONObject out = inout.getJSONObject("out");

                    String protocol = in.getString("protocol");
                    String deviceType = (in.has("deviceType") ? in.getString("deviceType") : "");
                    String url = out.getString("url");
                    synchronized (mRemoUrls) {
                        mRemoUrls.put(protocol, deviceType, url);
                    }
                } catch (JSONException e) {
                    continue;
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<JSONObject> loader) {
        }
    };

    public static DeviceListFragment newInstance(int cursorLoaderId, int jsonloaderId) {
        DeviceListFragment fragment = new DeviceListFragment();
        Bundle args = new Bundle();
        args.putInt(CURSOR_LOADER_ID_KEY, cursorLoaderId);
        args.putInt(JSON_LOADER_ID_KEY, jsonloaderId);
        fragment.setArguments(args);
        return fragment;
    }

    public DeviceListFragment() {
    }

    private int getCursorLoaderId() {
        return getArguments().getInt(CURSOR_LOADER_ID_KEY);
    }

    private int getJsonLoaderId() {
        return getArguments().getInt(JSON_LOADER_ID_KEY);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(activity, KadecotService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        getActivity().unbindService(mServiceConnection);
        super.onDetach();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = (Cursor) l.getItemAtPosition(position);

        if (cursor.getInt(cursor.getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.STATUS)) != 1) {
            return;
        }

        String protocol = cursor.getString(cursor
                .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL));
        String deviceType = cursor.getString(cursor
                .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE));
        long deviceId = cursor.getLong(cursor
                .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID));

        String url = null;
        synchronized (mRemoUrls) {
            url = mRemoUrls.get(protocol, deviceType);
            if (url == null) {
                url = mRemoUrls.get(protocol, "");
            }
        }
        if (url == null) {
            Toast.makeText(getActivity(), R.string.unsupported, Toast.LENGTH_SHORT).show();
            return;
        }

        WebAppLauncher.launch(getActivity(),
                url + "?kip=" + ConnectivityManagerUtil.getIPAddress(getActivity()) + "&id="
                        + deviceId);
    }

    private void disableAllDevices() {
        ContentProviderClient client = getActivity().getContentResolver()
                .acquireContentProviderClient(
                        KadecotCoreStore.Devices.CONTENT_URI);
        try {
            ContentValues values = new ContentValues();
            values.put(KadecotCoreStore.Devices.DeviceColumns.STATUS, 0);
            client.update(KadecotCoreStore.Devices.CONTENT_URI, values, null, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            client.release();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHttpClient = AndroidHttpClient.newInstance(AppListFragment.class.getSimpleName());
        getLoaderManager().initLoader(getCursorLoaderId(), null, mCursorLoaderCallbacks);
        getLoaderManager().initLoader(getJsonLoaderId(), null, mJsonLoaderCallbacks);
        getActivity().getContentResolver().registerContentObserver(
                KadecotCoreStore.Devices.CONTENT_URI, true, mDeviceObserver);
        getActivity().getContentResolver().registerContentObserver(
                KadecotCoreStore.DeviceTypes.CONTENT_URI, true, mDeviceTypeObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mInflater = inflater;
        View view = mInflater.inflate(R.layout.listview_device, container, false);
        final SwipeRefreshLayout layout = (SwipeRefreshLayout) view
                .findViewById(R.id.swipe_refresh_layout);
        layout.setColorSchemeColors(Color.YELLOW, Color.DKGRAY, Color.YELLOW, Color.DKGRAY);
        layout.setEnabled(false);
        layout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                final Runnable refreshed = new Runnable() {
                    @Override
                    public void run() {
                        layout.setRefreshing(false);
                    }
                };

                final Runnable refreshing = new Runnable() {
                    @Override
                    public void run() {
                        disableAllDevices();
                        Message msg = Message.obtain(null, KadecotService.MSGR_INTERFACE_VERSION,
                                mClientId, WampRequestIdGenerator.getId());
                        msg.getData().putString(KadecotService.MSGR_KEY_REQ_WAMP,
                                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(),
                                        new JSONObject(), KadecotWampTopic.TOPIC_PRIVATE_SEARCH)
                                        .toString());
                        if (mMessenger != null) {
                            try {
                                mMessenger.send(msg);
                            } catch (RemoteException e) {
                                // Never happens.
                                throw new IllegalStateException(
                                        "Can not send message through messenger.");
                            }
                        }
                        mHandler.postDelayed(refreshed, 2000);
                    }
                };
                layout.setEnabled(false);
                mHandler.post(refreshing);
            }
        });
        final ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
                int topRowVerticalPosition =
                        (listView == null || listView.getChildCount() == 0) ?
                                0 : listView.getChildAt(0).getTop();
                layout.setEnabled(topRowVerticalPosition >= 0);
            }
        });
        return view;
    }

    @Override
    public void setEmptyText(CharSequence text) {
        TextView tv = (TextView) getListView().getEmptyView();
        tv.setText(text);
    }

    @Override
    public void onDestroyView() {
        getActivity().getContentResolver().unregisterContentObserver(mDeviceTypeObserver);
        getActivity().getContentResolver().unregisterContentObserver(mDeviceObserver);
        getLoaderManager().destroyLoader(getJsonLoaderId());
        getLoaderManager().destroyLoader(getCursorLoaderId());
        mHttpClient.close();
        super.onDestroyView();
    }

    private static final class DeviceCursorAdapter extends CursorAdapter {

        private final LayoutInflater mInflater;
        private final Context mContext;
        private final Bitmap mDefaultIcon;

        public DeviceCursorAdapter(Context context, Cursor c, int flags, LayoutInflater inflater) {
            super(context, c, flags);
            mContext = context;
            mInflater = inflater;
            mDefaultIcon = BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.ic_action_help);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(R.layout.device_list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            bindBackgroundColor(view, context, cursor);
            bindDeviceIcon(view, context, cursor);
            bindDeviceName(view, context, cursor);
            bindMenu(view, context, cursor);
        }

        private void bindBackgroundColor(View view, Context context, Cursor cursor) {
            boolean status = cursor.getInt(cursor
                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.STATUS)) == 1;
            if (status) {
                view.setBackgroundColor(android.R.drawable.list_selector_background);
            } else {
                view.setBackgroundColor(Color.LTGRAY);
            }
        }

        private void bindDeviceIcon(View view, Context context, Cursor cursor) {
            final ImageView iconView = (ImageView) view.findViewById(R.id.deviceicon);
            final String protocol = cursor.getString(cursor
                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL));
            final String deviceType = cursor.getString(cursor
                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE));

            ContentProviderClient client = mContext.getContentResolver()
                    .acquireContentProviderClient(
                            KadecotCoreStore.DeviceTypes.CONTENT_URI);

            Bitmap icon = mDefaultIcon;
            try {
                Cursor c = client.query(KadecotCoreStore.DeviceTypes.CONTENT_URI,
                        new String[] {
                            KadecotCoreStore.DeviceTypes.DeviceTypeColumns.ICON
                        },
                        KadecotCoreStore.DeviceTypes.DeviceTypeColumns.PROTOCOL
                                + " =?" + " AND " +
                                KadecotCoreStore.DeviceTypes.DeviceTypeColumns.DEVICE_TYPE
                                + "=?",
                        new String[] {
                                protocol,
                                deviceType
                        }, null);

                if (c.getCount() == 1) {
                    c.moveToFirst();
                    byte[] iconByte = c
                            .getBlob(c
                                    .getColumnIndexOrThrow(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.ICON));
                    if (iconByte != null) {
                        icon = BitmapFactory.decodeByteArray(iconByte, 0, iconByte.length);
                    }
                }
                c.close();
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                client.release();
            }
            iconView.setImageBitmap(icon);
        }

        private void bindDeviceName(View view, Context context, Cursor cursor) {
            TextView nickname = (TextView) view.findViewById(R.id.text1);
            nickname.setText(cursor.getString(cursor
                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.NICKNAME)));
            TextView ipaddr = (TextView) view.findViewById(R.id.text2);
            ipaddr.setText(cursor.getString(cursor
                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.IP_ADDR)));
        }

        private void bindMenu(View view, Context context, Cursor cursor) {
            final String nickname = cursor.getString(cursor
                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.NICKNAME));
            final long deviceId = cursor.getLong(cursor
                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID));
            final String location = cursor.getString(cursor
                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.LOCATION));
            final String subLocation = cursor.getString(cursor
                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.SUB_LOCATION));
            final String protocol = cursor.getString(cursor
                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL));
            final String deviceType = cursor.getString(cursor
                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE));
            final String ipaddress = cursor.getString(cursor
                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.IP_ADDR));
            final int status = cursor.getInt(cursor
                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.STATUS));

            final ImageView menu = (ImageView) view.findViewById(R.id.menu);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(mContext, menu);
                    popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());
                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            final int id = item.getItemId();
                            if (id == R.id.change_nick_name) {
                                onChangeNickname(nickname, deviceId);
                                return true;
                            }
                            if (id == R.id.change_location) {
                                onChangeLocation(location, subLocation, deviceId);
                            }
                            if (id == R.id.launch_plugin_settings) {
                                onLaunchPluginSetting(protocol, deviceType, deviceId, nickname,
                                        ipaddress, status);
                                return true;
                            }
                            return false;
                        }
                    });
                }
            });
        }

        private void onChangeNickname(final String currentNickname, final long deviceId) {
            final EditText editText = new EditText(mContext);
            editText.setText(currentNickname);
            editText.selectAll();
            editText.setInputType(InputType.TYPE_CLASS_TEXT);

            Dialog dialog = new AlertDialog.Builder(mContext)
                    .setTitle(R.string.title_dialog_change_nickname)
                    .setView(editText)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    String newNickname = editText.getText()
                                            .toString();
                                    if (newNickname.length() == 0) {
                                        return;
                                    }
                                    if (newNickname.equals(currentNickname)) {
                                        return;
                                    }
                                    ContentProviderClient client = mContext
                                            .getContentResolver()
                                            .acquireContentProviderClient(
                                                    KadecotCoreStore.Devices.CONTENT_URI);
                                    ContentValues values = new ContentValues();
                                    values.put(
                                            KadecotCoreStore.Devices.DeviceColumns.NICKNAME,
                                            newNickname);
                                    try {
                                        client.update(
                                                KadecotCoreStore.Devices.CONTENT_URI,
                                                values,
                                                KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID
                                                        + " =?",
                                                new String[] {
                                                    String.valueOf(deviceId)
                                                });
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                        return;
                                    } finally {
                                        client.release();
                                    }

                                    Toast.makeText(mContext,
                                            R.string.success_change_nickname,
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }).create();
            dialog.getWindow()
                    .setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            dialog.show();
        }

        private void onChangeLocation(final String currentLocation,
                final String currentSubLocation, final long deviceId) {

            Dialog dialog = ChangeLocationDialogFactory.create(mContext, currentLocation,
                    currentSubLocation, deviceId);
            dialog.getWindow()
                    .setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            dialog.show();
        }

        public boolean hasPlugin(String packageName, String activityName) {
            try {
                mContext.getPackageManager().getActivityInfo(
                        new ComponentName(packageName, activityName), 0);
                return true;
            } catch (NameNotFoundException e) {
                return false;
            }
        }

        private void onLaunchPluginSetting(String protocol, String deviceType, Long deviceId,
                String nickname, String ipaddress, int status) {
            ContentProviderClient client = mContext.getContentResolver()
                    .acquireContentProviderClient(KadecotCoreStore.Protocols.CONTENT_URI);
            Cursor c;
            try {
                c = client.query(KadecotCoreStore.Protocols.CONTENT_URI,
                        new String[] {
                                KadecotCoreStore.Protocols.ProtocolColumns.PROTOCOL,
                                KadecotCoreStore.Protocols.ProtocolColumns.ACTIVITY_NAME,
                                KadecotCoreStore.Protocols.ProtocolColumns.PACKAGE_NAME
                        },
                        KadecotCoreStore.Protocols.ProtocolColumns.PROTOCOL
                                + "=?",
                        new String[] {
                            protocol
                        }, null);
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            } finally {
                client.release();
            }

            try {
                if (c.getCount() != 1) {
                    Toast.makeText(mContext, R.string.currently_unavailable, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                c.moveToFirst();
                String className = c
                        .getString(c
                                .getColumnIndexOrThrow(KadecotCoreStore.Protocols.ProtocolColumns.ACTIVITY_NAME));
                String packageName = c
                        .getString(c
                                .getColumnIndexOrThrow(KadecotCoreStore.Protocols.ProtocolColumns.PACKAGE_NAME));
                if (hasPlugin(packageName, className)) {
                    Intent intent = new Intent();
                    intent.setClassName(packageName, className);
                    intent.putExtra("nickname", nickname);
                    intent.putExtra("deviceType", deviceType);
                    intent.putExtra("deviceId", deviceId);
                    intent.putExtra("ipAddress", ipaddress);
                    intent.putExtra("status", status);
                    mContext.startActivity(intent);
                } else {
                    Toast.makeText(mContext, R.string.currently_unavailable,
                            Toast.LENGTH_SHORT).show();
                }
            } finally {
                c.close();
            }
        }
    }

    @Override
    public void onReceive(Message msg) {
        if (msg.what != KadecotService.MSGR_INTERFACE_VERSION) {
            return;
        }

        if (msg.getData().getBoolean(KadecotService.MSGR_KEY_CONNECT)) {
            mClientId = msg.arg1;

            Message wampHello = Message.obtain(null, KadecotService.MSGR_INTERFACE_VERSION,
                    mClientId, WampRequestIdGenerator.getId());
            wampHello.getData().putString(KadecotService.MSGR_KEY_REQ_WAMP,
                    WampMessageFactory.createHello("realm", new JSONObject()).toString());
            try {
                if (mMessenger != null) {
                    mMessenger.send(wampHello);
                }
            } catch (RemoteException e) {
                // Never happens.
                throw new IllegalStateException("Can not send messeage through messenger");
            }
        }
    }
}
