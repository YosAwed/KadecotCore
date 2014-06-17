
package com.sonycsl.wamp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DoubleKeyMap<W, K, V> {

    private Map<W, Map<K, V>> mKkvMap = new ConcurrentHashMap<W, Map<K, V>>();

    public V put(W key0, K key, V value) {
        Map<K, V> kv = mKkvMap.get(key0);
        if (kv == null) {
            kv = new ConcurrentHashMap<K, V>();
            mKkvMap.put(key0, kv);
        }
        return kv.put(key, value);
    }

    public V get(W key0, K key) {
        Map<K, V> kv = mKkvMap.get(key0);
        if (kv == null) {
            return null;
        }
        return kv.get(key);
    }

    public V remove(W key0, K key) {
        Map<K, V> kv = mKkvMap.get(key0);
        if (kv == null) {
            return null;
        }
        return kv.remove(key);
    }

    public boolean containsKey(W key0, K key) {
        Map<K, V> kv = mKkvMap.get(key0);
        if (kv == null) {
            return false;
        }
        return kv.containsKey(key);
    }

    public boolean isEmpty(W key0) {
        Map<K, V> kv = mKkvMap.get(key0);
        if (kv == null) {
            return true;
        }
        return kv.size() == 0;
    }
}
