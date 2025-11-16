package org.red.library;

import java.util.HashMap;
import java.util.Map;

import org.red.library.adapter.IAdapter;
import org.red.library.serialize.DataMapConverter;
import org.red.library.serialize.RegisterSerializable;
import org.red.library.serialize.SerializeDataMap;

public class DataMapManager {
    private static Map<Class<?>, RegisterSerializable> registerSerializableMap = new HashMap<>();

    public static <T> void registerSerializableClass(Class<T> clazz, RegisterSerializable registerSerializable) {
        registerSerializableMap.put(clazz, registerSerializable);
    }

    public static <T> RegisterSerializable getSerializableClass(Class<?> clazz) {
        return registerSerializableMap.getOrDefault(clazz, null);
    }

    public static boolean containSerializableClass(Class<?> clazz) {
        return registerSerializableMap.containsKey(clazz);
    }

    private final HashMap<String, IAdapter> map = new HashMap<>();
    private final DataMapConverter converter = new DataMapConverter();

    public void registerAdapter(String type, IAdapter adapter) {
        map.put(type, adapter);
    }

    public boolean contain(String type) {
        return map.containsKey(type);
    }

    public void delete(String type) {
        if (contain(type))
            map.remove(type);
    }

    public IAdapter get(String type) {
        return map.getOrDefault(type, null);
    }

    public void saveDataMap(String type, String key, DataMap map) {
        if (!contain(type))
            throw new IllegalStateException(type + " Adapter is Null");

        SerializeDataMap serMap = converter.serializeObject(map);
        get(type).saveDataMap(key, serMap);
    }

    public DataMap loadDataMap(String type, String key) {
        if (!contain(type))
            throw new IllegalStateException(type + " Adapter is Null");

        SerializeDataMap serMap = get(type).loadDataMap(key);
        return this.converter.deserializeObject(serMap, DataMap.class);
    }

    public void deleteDataMap(String type, String key) {
        if (!contain(type))
            throw new IllegalStateException(type + " Adapter is Null");

        get(type).deleteDataMap(key);
    }

    public boolean containDataMap(String type, String key) {
        if (!contain(type))
            throw new IllegalStateException(type + " Adapter is Null");

        return get(type).containDataMap(key);
    }
}
