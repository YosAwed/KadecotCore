
package com.sonycsl.wamp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DoubleKeyMap<K1, K2, V> {

    private Map<K1, Map<K2, V>> mKkvMap = new ConcurrentHashMap<K1, Map<K2, V>>();

    public V put(K1 key1, K2 key2, V value) {
        Map<K2, V> kv = mKkvMap.get(key1);
        if (kv == null) {
            kv = new ConcurrentHashMap<K2, V>();
            mKkvMap.put(key1, kv);
        }
        return kv.put(key2, value);
    }

    public V get(K1 key1, K2 key2) {
        Map<K2, V> kv = mKkvMap.get(key1);
        if (kv == null) {
            return null;
        }
        return kv.get(key2);
    }

    public V remove(K1 key1, K2 key2) {
        Map<K2, V> kv = mKkvMap.get(key1);
        if (kv == null) {
            return null;
        }
        return kv.remove(key2);
    }

    public boolean containsKey(K1 key1, K2 key2) {
        Map<K2, V> kv = mKkvMap.get(key1);
        if (kv == null) {
            return false;
        }
        return kv.containsKey(key2);
    }

    public void clear() {
        for (K1 key1 : mKkvMap.keySet()) {
            Map<K2, V> kv = mKkvMap.get(key1);
            if (kv != null) {
                kv.clear();
            }
        }
        mKkvMap.clear();
    }

}
