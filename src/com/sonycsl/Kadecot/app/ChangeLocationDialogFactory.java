
package com.sonycsl.Kadecot.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.sonycsl.Kadecot.core.R;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.provider.KadecotLocation;

public class ChangeLocationDialogFactory {
    public static Dialog create(final Context context, final String currentLocation,
            final String currentSubLocation, final long deviceId) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout layout = (LinearLayout) inflater.inflate(
                R.layout.change_location_dialog, null);
        final Spinner spinner = (Spinner) layout.findViewById(R.id.location_spinner);
        spinner.setSelection(getCurrentLocationPosition(context,
                KadecotLocation.toLanguageWord(currentLocation, context)));
        final EditText editText = (EditText) layout.findViewById(R.id.sub_location);
        editText.setText(currentSubLocation);

        return new AlertDialog.Builder(context)
                .setTitle(R.string.title_dialog_change_location)
                .setView(layout)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                String newMainLocation = (String) spinner.getSelectedItem();
                                String newSubLocation = editText.getText().toString();

                                if (newMainLocation.equals(currentLocation)
                                        && newSubLocation.equals(currentSubLocation)) {
                                    return;
                                }
                                ContentProviderClient client = context
                                        .getContentResolver()
                                        .acquireContentProviderClient(
                                                KadecotCoreStore.Devices.CONTENT_URI);
                                ContentValues values = new ContentValues();
                                values.put(
                                        KadecotCoreStore.Devices.DeviceColumns.LOCATION,
                                        newMainLocation);
                                values.put(KadecotCoreStore.Devices.DeviceColumns.SUB_LOCATION,
                                        newSubLocation);
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

                                Toast.makeText(context,
                                        R.string.success_change_location,
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }).create();

    }

    private static int getCurrentLocationPosition(Context context, final String currentLocation) {
        TypedArray ta = context.getResources().obtainTypedArray(R.array.location_label);
        for (int i = 0; i < ta.length(); i++) {
            if (currentLocation.equals(ta.getString(i))) {
                return i;
            }
        }

        // return "Others" position
        return ta.length() - 1;
    }
}
