/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.provider;

import android.content.Context;

import com.sonycsl.Kadecot.core.R;

public enum KadecotLocation {
    LIVING_ROOM("Living room", R.string.living_room),
    DINING_ROOM("Dining room", R.string.dining_room),
    KITCHEN("Kitchen", R.string.kitchen),
    BATHROOM("Bathroom", R.string.bathroom),
    LAVATORY("Lavatory", R.string.lavatory),
    WASHROOM("Washroom/changing room", R.string.washroom_changing_room),
    PASSAGEWAY("Passageway", R.string.passageway),
    ROOM("Room", R.string.room),
    STAIRWAY("Stairway", R.string.stairway),
    FRONT_DOOR("Front door", R.string.front_door),
    STOREROOM("Storeroom", R.string.storeroom),
    GARDEN("Garden/perimeter", R.string.garden_perimiter),
    GARAGE("Garage", R.string.garage),
    VERANDA("Veranda/balcony", R.string.veranda_balcony),
    OTHERS("Others", R.string.others);

    String mName;
    int mResourceId;

    private KadecotLocation(String name, int resourceId) {
        mName = name;
        mResourceId = resourceId;
    }

    public String getName() {
        return mName;
    }

    public int getResourceId() {
        return mResourceId;
    }

    public static String toLanguageWord(String location, Context context) {
        for (KadecotLocation l : KadecotLocation.values()) {
            if (location.equals(l.getName())) {
                return context.getResources().getString(l.getResourceId());
            }
        }

        return context.getResources().getString(OTHERS.getResourceId());
    }

    public static String toEnglish(String location, Context context) {
        for (KadecotLocation l : KadecotLocation.values()) {
            if (location.equals(context.getResources().getString(l.getResourceId()))) {
                return l.getName();
            }
        }

        return OTHERS.getName();
    }
}
