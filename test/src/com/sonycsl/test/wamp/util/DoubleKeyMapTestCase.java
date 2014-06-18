
package com.sonycsl.test.wamp.util;

import com.sonycsl.test.mock.MockWampPeer;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.util.DoubleKeyMap;

import junit.framework.TestCase;

import org.json.JSONObject;

public class DoubleKeyMapTestCase extends TestCase {

    private DoubleKeyMap<WampPeer, Integer, WampMessage> mKm;
    private MockWampPeer[] mKeys1 = {
            new MockWampPeer(),
            new MockWampPeer()
    };
    private int[] mKeys2 = {
            100, 200
    };

    @Override
    protected void setUp() {
        mKm = new DoubleKeyMap<WampPeer, Integer, WampMessage>();
    }

    public void testCtor() {
        assertNotNull(new DoubleKeyMap<WampPeer, Integer, WampMessage>());
    }

    public void testPut() {
        for (MockWampPeer key1 : mKeys1) {
            for (int key2 : mKeys2) {
                WampMessage pastMsg = null;
                for (int i = 0; i < 2; i++) {
                    WampMessage msg = WampMessageFactory.createHello("realm", new JSONObject());
                    assertEquals(pastMsg, mKm.put(key1, key2, msg));
                    pastMsg = msg;
                }
            }
        }
    }

    public void testGet() {
        for (MockWampPeer key1 : mKeys1) {
            for (int key2 : mKeys2) {
                WampMessage pastMsg = null;
                for (int i = 0; i < 2; i++) {
                    WampMessage msg = WampMessageFactory.createHello("realm", new JSONObject());
                    assertEquals(pastMsg, mKm.put(key1, key2, msg));
                    pastMsg = msg;

                    assertEquals(msg, mKm.get(key1, key2));
                }
            }
        }
    }

    public void testRemove() {
        for (MockWampPeer key1 : mKeys1) {
            for (int key2 : mKeys2) {
                for (int i = 0; i < 2; i++) {
                    WampMessage msg = WampMessageFactory.createHello("realm", new JSONObject());
                    mKm.put(key1, key2, msg);
                    assertEquals(msg, mKm.remove(key1, key2));
                }
            }
        }

        for (MockWampPeer key1 : mKeys1) {
            for (int key2 : mKeys2) {
                assertEquals(null, mKm.get(key1, key2));
            }
        }
    }

    public void testContainsKey() {
        for (MockWampPeer key1 : mKeys1) {
            for (int key2 : mKeys2) {
                for (int i = 0; i < 2; i++) {
                    WampMessage msg = WampMessageFactory.createHello("realm", new JSONObject());
                    mKm.put(key1, key2, msg);
                }
            }
        }

        for (MockWampPeer key1 : mKeys1) {
            for (int key2 : mKeys2) {
                for (int i = 0; i < 2; i++) {
                    assertTrue(mKm.containsKey(key1, key2));
                }
            }
        }
    }

    public void testClear() {
        for (MockWampPeer key1 : mKeys1) {
            for (int key2 : mKeys2) {
                for (int i = 0; i < 2; i++) {
                    WampMessage msg = WampMessageFactory.createHello("realm", new JSONObject());
                    mKm.put(key1, key2, msg);
                }
            }
        }

        mKm.clear();

        for (MockWampPeer key1 : mKeys1) {
            for (int key2 : mKeys2) {
                for (int i = 0; i < 2; i++) {
                    assertFalse(mKm.containsKey(key1, key2));
                }
            }
        }
    }

}
