/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sonycsl.Kadecot.core.R;
import com.sonycsl.Kadecot.net.ConnectivityManagerUtil;
import com.sonycsl.Kadecot.net.NetworkID;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;

public class AccessPointListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    private static final String LOADER_ID_KEY = "loaderId";

    private LayoutInflater mInflater;

    public static AccessPointListFragment newInstance(int loaderId) {
        AccessPointListFragment fragment = new AccessPointListFragment();

        Bundle args = new Bundle();
        args.putInt(LOADER_ID_KEY, loaderId);
        fragment.setArguments(args);
        return fragment;
    }

    public AccessPointListFragment() {
    }

    private int getLoaderId() {
        return getArguments().getInt(LOADER_ID_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mInflater = inflater;
        View view = mInflater.inflate(R.layout.listview_access_point, container, false);

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
        return new CursorLoader(getActivity(), KadecotCoreStore.AccessPoints.CONTENT_URI,
                null, null, null, null);
    }

    private static String getSSID(Cursor cursor) {
        return cursor.getString(cursor
                .getColumnIndex(KadecotCoreStore.AccessPoints.AccessPointColumns.SSID));
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
                final String ssid = getSSID(cursor);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(ssid);

            }
        };

        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listview = (ListView) parent;
                Cursor cursor = (Cursor) listview.getItemAtPosition(position);
                String ssid = getSSID(cursor);
                NetworkID networkID = ConnectivityManagerUtil.getCurrentNetworkID(getActivity());

                if (networkID != null && ssid.equals(networkID.getSsid())) {
                    new CurrentSsidDialogFragment().show(getFragmentManager(), "currentSsidDialog");
                } else {
                    AccessPointDeleteDialogFragment.newInstance(ssid, getLoaderId(),
                            new RemoveAPListener() {

                                @Override
                                public void removeAP(String ssid) {
                                    removeAccessPointData(ssid);
                                }
                            }).show(
                            getFragmentManager(), "deleteAP");
                }
            }
        });

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

    public static final class CurrentSsidDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);

            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title_attention)
                    .setMessage(getString(R.string.message_current_ssid))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create();
            return dialog;
        }
    }

    public static final class AccessPointDeleteDialogFragment extends DialogFragment {
        private static final String SSID_KEY = "ssid";
        private static final String LOADER_ID_KEY = "loaderId";
        private static RemoveAPListener sListener;

        public static AccessPointDeleteDialogFragment newInstance(String ssid, int loaderId,
                RemoveAPListener listener) {
            AccessPointDeleteDialogFragment fragment = new AccessPointDeleteDialogFragment();
            Bundle args = new Bundle();
            args.putString(SSID_KEY, ssid);
            args.putInt(LOADER_ID_KEY, loaderId);
            fragment.setArguments(args);
            sListener = listener;
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);

            final String ssid = getArguments().getString(SSID_KEY);
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title_attention)
                    .setMessage(getString(R.string.message_delete_accesspoint, ssid))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sListener.removeAP(ssid);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            }).create();
            return dialog;
        }
    }

    public interface RemoveAPListener {
        void removeAP(String ssid);
    }

    private void removeAccessPointData(String ssid) {
        final int loaderId = getArguments().getInt(LOADER_ID_KEY);
        ContentProviderClient client = getActivity().getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.AccessPoints.CONTENT_URI);
        try {
            client.delete(KadecotCoreStore.AccessPoints.CONTENT_URI,
                    KadecotCoreStore.AccessPoints.AccessPointColumns.SSID + " = \'"
                            + ssid + "\'", null);
            Loader<Cursor> loader = getLoaderManager().getLoader(loaderId);
            loader.reset();
            loader.startLoading();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
