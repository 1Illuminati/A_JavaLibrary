package org.red.library.data;

import java.util.HashMap;
import java.util.Map;

import org.red.library.data.adapter.IAdapter;
import org.red.library.data.serialize.DataMapConverter;
import org.red.library.data.serialize.RegisterSerializable;
import org.red.library.data.serialize.SerializeDataMap;

public class DataMapManager {
    private final IAdapter adapter;
    private final DataMapConverter converter = new DataMapConverter(this);
    private Map<Class<?>, RegisterSerializable> registerSerializableMap = new HashMap<>();

    public DataMapManager(IAdapter adapter) {
        this.adapter = adapter;
    }

    public IAdapter getAdapter() {
        return this.adapter;
    }

    public <T> void registerSerializableClass(Class<T> clazz, RegisterSerializable<T> registerSerializable) {
        registerSerializableMap.put(clazz, registerSerializable);
    }

    public <T> RegisterSerializable getSerializableClass(Class<T> clazz) {
        return registerSerializableMap.getOrDefault(clazz, null);
    }

    public boolean containSerializableClass(Class<?> clazz) {
        return registerSerializableMap.containsKey(clazz);
    }

    public void saveDataMap(String key, DataMap map) {
        SerializeDataMap serMap = converter.serializeObject(map);
        adapter.saveDataMap(key, serMap);
    }

    public DataMap loadDataMap(String key) {
        SerializeDataMap serMap = adapter.loadDataMap(key);
        return this.converter.deserializeObject(serMap, DataMap.class);
    }

    public void deleteDataMap(String key) {
        adapter.deleteDataMap(key);
    }

    public boolean containDataMap(String key) {
        return adapter.containDataMap(key);
    }

    public DataMapConverter getConverter() {
        return this.getConverter();
    }
}
