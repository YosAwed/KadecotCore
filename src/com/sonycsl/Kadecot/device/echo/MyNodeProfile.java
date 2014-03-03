
package com.sonycsl.Kadecot.device.echo;

import com.sonycsl.echo.eoj.profile.NodeProfile;

import java.io.IOException;

public class MyNodeProfile extends NodeProfile {
    @SuppressWarnings("unused")
    private static final String TAG = MyNodeProfile.class.getSimpleName();

    byte[] mManufactureCode = {
            0, 0, 0
    };

    byte[] mStatus = {
            0x30
    };

    byte[] mVersion = {
            1, 1, 1, 0
    };

    byte[] mIdNumber = {
            (byte) 0xFE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    byte[] mUniqueId = {
            0, 0
    };

    @Override
    protected byte[] getManufacturerCode() {
        return mManufactureCode;
    }

    @Override
    protected byte[] getOperatingStatus() {
        return mStatus;
    }

    public void changeOperatingStatus(boolean status) {
        byte b = (status ? (byte) 0x30 : (byte) 0x31);

        if (mStatus[0] == b)
            return;
        mStatus[0] = b;
        try {
            inform().reqInformOperatingStatus().send();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected byte[] getVersionInformation() {
        return mVersion;
    }

    @Override
    protected byte[] getIdentificationNumber() {
        return mIdNumber;
    }

    @Override
    protected boolean setUniqueIdentifierData(byte[] edt) {
        if ((edt[0] & 0x40) != 0x40)
            return false;

        mUniqueId[0] = (byte) ((edt[0] & (byte) 0x7F) | (mUniqueId[0] & 0x80));
        mUniqueId[1] = edt[1];
        return true;
    }

    @Override
    protected byte[] getUniqueIdentifierData() {
        return mUniqueId;
    }

}
