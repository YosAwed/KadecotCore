/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.app;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sonycsl.Kadecot.content.JsonLoader;
import com.sonycsl.Kadecot.content.WifiConnectionBroadcastReceiver;
import com.sonycsl.Kadecot.core.R;
import com.sonycsl.Kadecot.core.R.layout;
import com.sonycsl.Kadecot.net.ConnectivityManagerUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppListFragment extends ListFragment implements LoaderCallbacks<JSONObject> {

    private static final String LOADER_ID_KEY = "loaderId";
    private AppsJsonAdapter mAdapter;
    private AndroidHttpClient mHttpClient;

    WifiConnectionBroadcastReceiver mReceiver;

    public static AppListFragment newInstance(int loaderId) {
        AppListFragment fragment = new AppListFragment();
        Bundle args = new Bundle();
        args.putInt(LOADER_ID_KEY, loaderId);
        fragment.setArguments(args);
        return fragment;
    }

    public AppListFragment() {
    }

    private int getLoaderId() {
        return getArguments().getInt(LOADER_ID_KEY);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHttpClient = AndroidHttpClient.newInstance(AppListFragment.class.getSimpleName());
        getLoaderManager().initLoader(getLoaderId(), null, this);
        mReceiver = new WifiConnectionBroadcastReceiver(getActivity()) {

            @Override
            public void onConnected() {
                getLoaderManager().restartLoader(getLoaderId(), null, AppListFragment.this);
            }

            @Override
            public void onDisconnected() {
                getLoaderManager().destroyLoader(getLoaderId());
                setListAdapter(new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1));
                setEmptyText(getString(R.string.no_application_available));
            }

        };
        getActivity().registerReceiver(mReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(mReceiver);
        getLoaderManager().destroyLoader(getLoaderId());
        mHttpClient.close();
        super.onDestroyView();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final String url;
        try {
            url = mAdapter.getUrl(position);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        WebAppLauncher.launch(getActivity(), url);
    }

    @Override
    public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
        JsonLoader loader = new JsonLoader(getActivity(), mHttpClient,
                getString(R.string.apps_json_url));
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
        JSONObject[] objects = new JSONObject[length];
        for (int i = 0; i < length; i++) {
            try {
                objects[i] = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }
        mAdapter = new AppsJsonAdapter(getActivity(), objects);
        setListAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<JSONObject> loader) {
    }

    private static final class AppsJsonAdapter extends ArrayAdapter<JSONObject> {

        private static final String ICON_KEY = "icon";
        private static final String TITLE_KEY = "title";
        private static final String DESCRIPTION_KEY = "description";
        private static final String URL_KEY = "url";

        private final Context mContext;
        private final LayoutInflater mInflater;
        private final Bitmap mDefaultIcon;

        public AppsJsonAdapter(Context context, JSONObject[] objects) {
            super(context, layout.app_list_item, objects);
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mDefaultIcon = BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.ic_action_help);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = mInflater.inflate(layout.app_list_item, parent, false);
            }
            try {
                if (getItem(position).has(ICON_KEY)) {
                    ((ImageView) (v.findViewById(R.id.deviceicon)))
                            .setImageBitmap(getAppImage(getItem(position).getString(ICON_KEY)));
                } else {
                    ((ImageView) (v.findViewById(R.id.deviceicon)))
                            .setImageBitmap(mDefaultIcon);
                }
                ((TextView) (v.findViewById(android.R.id.text1))).setText(getItem(position)
                        .getString(TITLE_KEY));
                ((TextView) (v.findViewById(android.R.id.text2))).setText(getItem(position)
                        .getString(DESCRIPTION_KEY));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return v;
        }

        public Bitmap getAppImage(String data) {
            data = data.replaceFirst("data:image/png;base64,", "");
            byte[] bytes = Base64.decode(data, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

        public String getUrl(int position) throws JSONException {
            return getItem(position).getString(URL_KEY) + "?kip="
                    + ConnectivityManagerUtil.getIPAddress(mContext);
        }
    }
}
