package org.red.library.data.adapter;

import java.util.Set;

import org.red.library.data.exception.KeyNotFoundException;
import org.red.library.data.serialize.SerializeDataMap;

public interface IAdapter {
    
    SerializeDataMap loadDataMap(String key) throws KeyNotFoundException;

    void saveDataMap(String key, SerializeDataMap value);

    boolean containDataMap(String key);

    void deleteDataMap(String key);

    Set<String> loadAllKey();
}
