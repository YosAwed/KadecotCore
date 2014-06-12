/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.Kadecot.database;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.sonycsl.Kadecot.database.KadecotDAO;
import com.sonycsl.Kadecot.wamp.KadecotProviderUtil;

import org.json.JSONArray;
import org.json.JSONException;

public class KadecotDAOTestCase extends AndroidTestCase {

    private static final String[] TEST_PROTOCOLS = {
            "test_protocol_1",
            "test_protocol_2"
    };

    private static final String[] TEST_UUIDS = {
            "test_uuid_1",
            "test_uuid_2"
    };

    private static final String[] TEST_DEVICE_TYPES = {
            "test_device_type_1",
            "test_device_type_2"
    };

    private static final String[] TEST_DESCRIPTIONS = {
            "test_description_1",
            "test_description_2"
    };

    private static final String[] TEST_TOPIC_NAMES = {
            "com.sonycsl.kadecot." + TEST_PROTOCOLS[0] + ".topic.A",
            "com.sonycsl.kadecot." + TEST_PROTOCOLS[0] + ".topic.B",
            "com.sonycsl.kadecot." + TEST_PROTOCOLS[1] + ".topic.A",
            "com.sonycsl.kadecot." + TEST_PROTOCOLS[1] + ".topic.B",
    };

    private static final String[] TEST_TOPIC_DESCS = {
            "A is a topic of " + TEST_PROTOCOLS[0],
            "B is a topic of " + TEST_PROTOCOLS[0],
            "A is a topic of " + TEST_PROTOCOLS[1],
            "B is a topic of " + TEST_PROTOCOLS[1],
    };

    private static final String[] TEST_PROCEDURE_NAMES = {
            "com.sonycsl.kadecot." + TEST_PROTOCOLS[0] + ".procedure.A",
            "com.sonycsl.kadecot." + TEST_PROTOCOLS[0] + ".procedure.B",
            "com.sonycsl.kadecot." + TEST_PROTOCOLS[1] + ".procedure.A",
            "com.sonycsl.kadecot." + TEST_PROTOCOLS[1] + ".procedure.B",
    };

    private static final String[] TEST_PROCEDURE_DESCS = {
            "A is a procedure of " + TEST_PROTOCOLS[0],
            "B is a procedure of " + TEST_PROTOCOLS[0],
            "A is a procedure of " + TEST_PROTOCOLS[1],
            "B is a procedure of " + TEST_PROTOCOLS[1],
    };

    private KadecotDAO mDao;

    @Override
    protected void setUp() throws Exception {
        mDao = new KadecotDAO(new RenamingDelegatingContext(getContext(), "test_"));
    }

    public void testCtor() {
        assertNotNull(mDao);
    }

    public void testPutDevice() {
        final long id0 = mDao.putDevice(TEST_PROTOCOLS[0], TEST_UUIDS[0], TEST_DEVICE_TYPES[0],
                TEST_DESCRIPTIONS[0], true);
        assertEquals(1, id0);

        final long id1 = mDao.putDevice(TEST_PROTOCOLS[1], TEST_UUIDS[1], TEST_DEVICE_TYPES[1],
                TEST_DESCRIPTIONS[1], true);
        assertEquals(2, id1);

        long id = mDao.putDevice(TEST_PROTOCOLS[0], TEST_UUIDS[0], TEST_DEVICE_TYPES[0],
                TEST_DESCRIPTIONS[0],
                false);
        assertEquals(id0, id);

        id = mDao.putDevice(TEST_PROTOCOLS[1], TEST_UUIDS[1], TEST_DEVICE_TYPES[1],
                TEST_DESCRIPTIONS[1],
                false);
        assertEquals(id1, id);
    }

    public void testRemoveDevice() {
        long id0 = mDao.putDevice(TEST_PROTOCOLS[0], TEST_UUIDS[0], TEST_DEVICE_TYPES[0],
                TEST_DESCRIPTIONS[0], true);
        assertEquals(1, id0);

        long id1 = mDao.putDevice(TEST_PROTOCOLS[1], TEST_UUIDS[1], TEST_DEVICE_TYPES[1],
                TEST_DESCRIPTIONS[1], true);
        assertEquals(2, id1);

        mDao.removeDevice(id0);
        mDao.removeDevice(id1);
    }

    public void testGetDeviceList() {
        long[] id = {
                mDao.putDevice(TEST_PROTOCOLS[0], TEST_UUIDS[0], TEST_DEVICE_TYPES[0],
                        TEST_DESCRIPTIONS[0], true),
                mDao.putDevice(TEST_PROTOCOLS[1], TEST_UUIDS[1], TEST_DEVICE_TYPES[1],
                        TEST_DESCRIPTIONS[1], true)
        };

        assertEquals(1, id[0]);
        assertEquals(2, id[1]);

        final JSONArray deviceList = mDao.getDeviceList(KadecotProviderUtil.DEVICE_ID,
                KadecotProviderUtil.DEVICE_PROTOCOL, KadecotProviderUtil.DEVICE_TYPE,
                KadecotProviderUtil.DEVICE_DESCRIPTION, KadecotProviderUtil.DEVICE_STATUS,
                KadecotProviderUtil.DEVICE_NICKNAME);
        assertEquals(2, deviceList.length());
        for (int i = 0; i < deviceList.length(); i++) {
            try {
                assertEquals(id[i],
                        deviceList.getJSONObject(i).getInt(KadecotProviderUtil.DEVICE_ID));
                assertEquals(
                        TEST_PROTOCOLS[i],
                        deviceList.getJSONObject(i).getString(
                                KadecotProviderUtil.DEVICE_PROTOCOL));
                assertEquals(
                        TEST_DEVICE_TYPES[i],
                        deviceList.getJSONObject(i).getString(
                                KadecotProviderUtil.DEVICE_TYPE));
                assertEquals(
                        TEST_DESCRIPTIONS[i],
                        deviceList.getJSONObject(i).getString(
                                KadecotProviderUtil.DEVICE_DESCRIPTION));
                assertTrue(deviceList.getJSONObject(i)
                        .getBoolean(KadecotProviderUtil.DEVICE_STATUS));
                assertEquals(
                        TEST_DESCRIPTIONS[i],
                        deviceList.getJSONObject(i).getString(
                                KadecotProviderUtil.DEVICE_NICKNAME));
            } catch (JSONException e) {
                fail(e.toString());
            }
        }
    }

    public void testChangeNickname() {
        String[] nickname = {
                "nickname1",
                "nickname2",
        };
        long[] id = {
                mDao.putDevice(TEST_PROTOCOLS[0], TEST_UUIDS[0], TEST_DEVICE_TYPES[0],
                        TEST_DESCRIPTIONS[0], true),
                mDao.putDevice(TEST_PROTOCOLS[1], TEST_UUIDS[1], TEST_DEVICE_TYPES[1],
                        TEST_DESCRIPTIONS[1], true)
        };

        assertEquals(1, id[0]);
        assertEquals(2, id[1]);

        mDao.changeNickname(id[0], nickname[0]);
        mDao.changeNickname(id[1], nickname[1]);

        final JSONArray deviceList = mDao.getDeviceList(KadecotProviderUtil.DEVICE_ID,
                KadecotProviderUtil.DEVICE_PROTOCOL, KadecotProviderUtil.DEVICE_TYPE,
                KadecotProviderUtil.DEVICE_DESCRIPTION, KadecotProviderUtil.DEVICE_STATUS,
                KadecotProviderUtil.DEVICE_NICKNAME);
        assertEquals(2, deviceList.length());
        for (int i = 0; i < deviceList.length(); i++) {
            try {
                assertEquals(id[i],
                        deviceList.getJSONObject(i).getInt(KadecotProviderUtil.DEVICE_ID));
                assertEquals(
                        nickname[i],
                        deviceList.getJSONObject(i).getString(
                                KadecotProviderUtil.DEVICE_NICKNAME));
            } catch (JSONException e) {
                fail(e.toString());
            }
        }

    }

    public void testPutTopic() {
        int length = TEST_TOPIC_NAMES.length;
        for (int i = 0; i < length; i++) {
            mDao.putTopic(TEST_TOPIC_NAMES[i], TEST_TOPIC_DESCS[i]);
        }
    }

    public void testRemoveTopic() {
        int length = TEST_TOPIC_NAMES.length;
        for (int i = 0; i < length; i++) {
            mDao.putTopic(TEST_TOPIC_NAMES[i], TEST_TOPIC_DESCS[i]);
        }
        for (int i = 0; i < length; i++) {
            mDao.removeTopic(TEST_TOPIC_NAMES[i]);
        }
    }

    public void testGetTopics() {
        int length = TEST_TOPIC_NAMES.length;

        for (int i = 0; i < length; i++) {
            mDao.putTopic(TEST_TOPIC_NAMES[i], TEST_TOPIC_DESCS[i]);
        }

        length = TEST_PROTOCOLS.length;
        for (int i = 0; i < length; i++) {
            JSONArray topics = mDao.getTopicList(TEST_PROTOCOLS[i]);
            try {
                assertEquals(TEST_TOPIC_NAMES[i * 2], topics.getString(0));
                assertEquals(TEST_TOPIC_NAMES[i * 2 + 1], topics.getString(1));
            } catch (JSONException e) {
                fail();
            }

        }

        length = TEST_TOPIC_NAMES.length;
        for (int i = 0; i < length; i++) {
            mDao.removeTopic(TEST_TOPIC_NAMES[i]);
        }

        length = TEST_PROTOCOLS.length;
        for (int i = 0; i < length; i++) {
            JSONArray topic = mDao.getTopicList(TEST_PROTOCOLS[i]);
            assertEquals(0, topic.length());
        }
    }

    public void testPutProcedure() {
        int length = TEST_PROCEDURE_NAMES.length;
        for (int i = 0; i < length; i++) {
            mDao.putProcedure(TEST_PROCEDURE_NAMES[i], TEST_PROCEDURE_DESCS[i]);
        }
    }

    public void testRemoveProcedure() {
        int length = TEST_PROCEDURE_NAMES.length;
        for (int i = 0; i < length; i++) {
            mDao.putProcedure(TEST_PROCEDURE_NAMES[i], TEST_PROCEDURE_DESCS[i]);
        }
        for (int i = 0; i < length; i++) {
            mDao.removeProcedure(TEST_PROCEDURE_NAMES[i]);
        }
    }

    public void testGetProcedures() {
        int length = TEST_PROCEDURE_NAMES.length;

        for (int i = 0; i < length; i++) {
            mDao.putProcedure(TEST_PROCEDURE_NAMES[i], TEST_PROCEDURE_DESCS[i]);
        }

        length = TEST_PROTOCOLS.length;
        for (int i = 0; i < length; i++) {
            JSONArray procedure = mDao.getProcedureList(TEST_PROTOCOLS[i]);
            try {
                assertEquals(TEST_PROCEDURE_NAMES[i * 2], procedure.getString(0));
                assertEquals(TEST_PROCEDURE_NAMES[i * 2 + 1], procedure.getString(1));
            } catch (JSONException e) {
                fail();
            }
        }

        length = TEST_PROCEDURE_NAMES.length;
        for (int i = 0; i < length; i++) {
            mDao.removeProcedure(TEST_PROCEDURE_NAMES[i]);
        }

        length = TEST_PROTOCOLS.length;
        for (int i = 0; i < length; i++) {
            JSONArray procedure = mDao.getProcedureList(TEST_PROTOCOLS[i]);
            assertEquals(0, procedure.length());
        }
    }

}
