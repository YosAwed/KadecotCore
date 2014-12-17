/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.sonycsl.Kadecot.core.R;
import com.sonycsl.Kadecot.preference.AccountSettingsPreference;
import com.sonycsl.Kadecot.preference.HelpPreference;

public class AccountSettingsDialogFragment extends DialogFragment {

    private static final String TITLE_KEY = "title";

    public static AccountSettingsDialogFragment getInstance(String title) {
        AccountSettingsDialogFragment fragment = new AccountSettingsDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_accountsettings, null);
        final EditText name = (EditText) view.findViewById(R.id.accound_name);
        final EditText pass = (EditText) view.findViewById(R.id.accound_pass);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString(TITLE_KEY))
                .setView(view)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AccountSettingsPreference.setName(getActivity(), name.getText().toString());
                        AccountSettingsPreference.setPass(getActivity(), pass.getText().toString());
                        HelpPreference.set(getActivity(), true);
                    }
                }).create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

                name.setText(AccountSettingsPreference.getName(getActivity()));
                pass.setText(AccountSettingsPreference.getPass(getActivity()));

                if (name.getText().length() == 0) {
                    button.setEnabled(false);
                    return;
                }
                if (pass.getText().length() == 0) {
                    button.setEnabled(false);
                    return;
                }
                button.setEnabled(true);
            }
        });

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (name.getText().length() == 0) {
                    button.setEnabled(false);
                    return;
                }
                if (pass.getText().length() == 0) {
                    button.setEnabled(false);
                    return;
                }
                button.setEnabled(true);
            }
        };
        name.addTextChangedListener(watcher);
        pass.addTextChangedListener(watcher);

        setCancelable(false);
        return dialog;
    }
}
