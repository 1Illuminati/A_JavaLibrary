package org.red.library.data;

import java.util.HashMap;
import java.util.Map;

import org.red.library.data.adapter.IAdapter;
import org.red.library.data.serialize.DataMapConverter;
import org.red.library.data.serialize.RegisterSerializable;
import org.red.library.data.serialize.SerializeDataMap;

public class DataMapManager {
    private final HashMap<String, IAdapter> map = new HashMap<>();
    private final DataMapConverter converter = new DataMapConverter(this);
    private Map<Class<?>, RegisterSerializable> registerSerializableMap = new HashMap<>();

    public <T> void registerSerializableClass(Class<T> clazz, RegisterSerializable<T> registerSerializable) {
        registerSerializableMap.put(clazz, registerSerializable);
    }

    public <T> RegisterSerializable getSerializableClass(Class<T> clazz) {
        return registerSerializableMap.getOrDefault(clazz, null);
    }

    public boolean containSerializableClass(Class<?> clazz) {
        return registerSerializableMap.containsKey(clazz);
    }

    public void registerAdapter(String type, IAdapter adapter) {
        map.put(type, adapter);
    }

    public boolean contain(String type) {
        return map.containsKey(type);
    }

    public void delete(String type) {
        if (contain(type)) map.remove(type);
    }

    public IAdapter get(String type) {
        return map.getOrDefault(type, null);
    }

    public void saveDataMap(String type, String key, DataMap map) {
        if (!contain(type)) throw new IllegalStateException(type + " Adapter is Null");

        SerializeDataMap serMap = converter.serializeObject(map);
        get(type).saveDataMap(key, serMap);
    }

    public DataMap loadDataMap(String type, String key) {
        if (!contain(type)) throw new IllegalStateException(type + " Adapter is Null");

        SerializeDataMap serMap = get(type).loadDataMap(key);
        return this.converter.deserializeObject(serMap, DataMap.class);
    }

    public void deleteDataMap(String type, String key) {
        if (!contain(type)) throw new IllegalStateException(type + " Adapter is Null");
        get(type).deleteDataMap(key);
    }

    public boolean containDataMap(String type, String key) {
        if (!contain(type)) throw new IllegalStateException(type + " Adapter is Null");
        return get(type).containDataMap(key);
    }

    public DataMapConverter getConverter() {
        return this.getConverter();
    }
}
