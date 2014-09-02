/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.app;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.sonycsl.Kadecot.core.R;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;

public class PluginListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    private static final String LOADER_ID_KEY = "loaderId";

    private LayoutInflater mInflater;
    private final Handler mHandler = new Handler();

    public static PluginListFragment newInstance(int loaderId) {
        PluginListFragment fragment = new PluginListFragment();
        Bundle args = new Bundle();
        args.putInt(LOADER_ID_KEY, loaderId);
        fragment.setArguments(args);
        return fragment;
    }

    public PluginListFragment() {
    }

    private int getLoaderId() {
        return getArguments().getInt(LOADER_ID_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mInflater = inflater;
        View view = mInflater.inflate(R.layout.listview_device, container, false);
        final SwipeRefreshLayout layout = (SwipeRefreshLayout) view
                .findViewById(R.id.swipe_refresh_layout);
        layout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        layout.setRefreshing(false);
                    }
                });
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(getLoaderId(), null, this);
    }

    @Override
    public void setEmptyText(CharSequence text) {
        TextView tv = (TextView) getListView().getEmptyView();
        tv.setText(text);
    }

    @Override
    public void onDestroyView() {
        getLoaderManager().destroyLoader(getLoaderId());
        super.onDestroyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new CursorLoader(getActivity(), KadecotCoreStore.Protocols.CONTENT_URI,
                null, null, null, null);
    }

    private static String getProtocolName(Cursor cursor) {
        return cursor.getString(cursor
                .getColumnIndex(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.PROTOCOL));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        final CursorAdapter cursorAdapter = new CursorAdapter(getActivity(), cursor, 0) {

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
                return mInflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false);
            }

            @Override
            public void bindView(View view, final Context context, Cursor cursor) {
                final String protocolName = getProtocolName(cursor);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(protocolName);

            }
        };
        setListAdapter(cursorAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        CursorAdapter adapter = (CursorAdapter) getListAdapter();
        Cursor old = adapter.swapCursor(null);
        if (old != null) {
            old.close();
        }
    }

}
