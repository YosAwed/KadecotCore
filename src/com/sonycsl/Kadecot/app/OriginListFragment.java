/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.app;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.sonycsl.Kadecot.provider.KadecotCoreStore;

public class OriginListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    private static final String LOADER_ID_KEY = "loaderId";

    private LayoutInflater mInflater;

    public static OriginListFragment newInstance(int loaderId) {
        OriginListFragment fragment = new OriginListFragment();
        Bundle args = new Bundle();
        args.putInt(LOADER_ID_KEY, loaderId);
        fragment.setArguments(args);
        return fragment;
    }

    public OriginListFragment() {
    }

    private int getLoaderId() {
        return getArguments().getInt(LOADER_ID_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(getLoaderId(), null, this);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        getListView().setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        getListView().setClickable(false);
        getListView().setFocusable(false);
        getListView().setFocusableInTouchMode(false);
    }

    @Override
    public void onDestroyView() {
        getLoaderManager().destroyLoader(getLoaderId());
        super.onDestroyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), KadecotCoreStore.Handshakes.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        CursorAdapter adapter = new CursorAdapter(getActivity(), cursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
                View view = mInflater.inflate(android.R.layout.simple_list_item_multiple_choice,
                        viewGroup, false);
                CheckedTextView textView = (CheckedTextView) view;
                textView.setClickable(true);
                textView.setFocusable(true);
                textView.setFocusableInTouchMode(true);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckedTextView textView = (CheckedTextView) v;
                        textView.toggle();
                        ContentProviderClient client = getActivity().getContentResolver()
                                .acquireContentProviderClient(
                                        KadecotCoreStore.Handshakes.CONTENT_URI);
                        ContentValues values = new ContentValues();
                        values.put(KadecotCoreStore.Handshakes.HandshakeColumns.STATUS,
                                textView.isChecked() ? 1 : 0);
                        try {
                            client.update(KadecotCoreStore.Handshakes.CONTENT_URI, values,
                                    KadecotCoreStore.Handshakes.HandshakeColumns.ORIGIN + "=?",
                                    new String[] {
                                        String.valueOf(textView.getText())
                                    });
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } finally {
                            client.release();
                        }
                    }
                });
                return view;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                CheckedTextView textView = (CheckedTextView) view;
                textView.setText(cursor.getString(cursor
                        .getColumnIndex(KadecotCoreStore.Handshakes.HandshakeColumns.ORIGIN)));

                boolean isChecked = cursor.getInt(cursor
                        .getColumnIndex(KadecotCoreStore.Handshakes.HandshakeColumns.STATUS)) == 1;
                // No effect. I don't know why...
                textView.setChecked(isChecked);
                // workaround
                getListView().setItemChecked(cursor.getPosition(), isChecked);
            }

        };
        setListAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }

}
