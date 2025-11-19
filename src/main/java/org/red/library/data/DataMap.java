package org.red.library.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.red.library.data.serialize.DataMapSerializable;

import java.util.Set;
import java.util.UUID;

public class DataMap implements DataMapSerializable {
    
    public static DataMap deserialize(DataMap map) {
        DataMap result = new DataMap();
        result.getMap().putAll(map.getMap());
        return result;
    }

    private Map<String, Object> map = new HashMap<>();

    public boolean isSerialze() {
        return false;
    }

    public void copy(DataMap dataMap) {
        map = dataMap.getMap();
    }

    public void copy(Map<String, Object> map) {
        this.map = map;
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int nullValue) {
        return (int) this.get(key, nullValue);
    }

    public void addInt(String key, int value) {
        put(key, getInt(key) + value);
    }

    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    public double getDouble(String key, double nullValue) {
        return (double) this.get(key, nullValue);
    }

    public void addDouble(String key, double value) {
        put(key, getDouble(key) + value);
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public String getString(String key, String nullValue) {
        Object obj = this.get(key, nullValue);
        if (obj == null) return "";
        return obj.toString();
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean nullValue) {
        return (boolean) this.get(key, nullValue);
    }

    public Object get(String key) {
        return this.get(key, null);
    }

    public Object get(String key, Object nullValue) {
        if (key.contains(".")) throw new IllegalArgumentException("key cannot contain '.'");
        if (!map.containsKey(key))
            put(key, nullValue);

        return map.get(key);
    }

    public <T> T getClass(String key, Class<T> clazz) {
        return this.getClass(key, clazz, null);
    }

    public <T> T getClass(String key, Class<T> clazz, Object nullValue) {
        if (!map.containsKey(key))
            put(key, nullValue);

        return clazz.cast(map.get(key));
    }

    public <T> List<T> getList(String key) {
        return this.getList(key, new ArrayList<>());
    }

    public <T> List<T> getList(String key, List<T> nullValue) {
        return (List<T>) this.get(key, nullValue);
    }

    public DataMap getDataMap(String key) {
        return this.getDataMap(key, new DataMap());
    }

    public DataMap getDataMap(String key, DataMap nullValue) {
        return (DataMap) this.get(key, nullValue);
    }

    public UUID getUUID(String key) {
        return this.getUUID(key, UUID.randomUUID());
    }

    public UUID getUUID(String key, UUID nullValue) {
        return (UUID) this.get(key, nullValue);
    }

    public void put(String key, Object value) {
        if (key.contains(".")) throw new IllegalArgumentException("key cannot contain '.'");
        map.put(key, value);
    }
    
    public DataMap set(String key, Object value) {
        put(key, value);
        return this;
    }

    public void remove(String key) {
        map.remove(key);
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Collection<Object> values() {
        return map.values();
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    public String toString() {
        return map.toString();
    }

    public void clear() {
        this.map.clear();
    }

    @Override
    public DataMap serialize() {
        return this;
    }
}

