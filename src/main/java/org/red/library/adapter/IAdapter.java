package org.red.library.adapter;

import org.red.library.serialize.SerializeDataMap;

public interface IAdapter {
    
    SerializeDataMap loadDataMap(String key);

    void saveDataMap(String key, SerializeDataMap value);

    boolean containDataMap(String key);

    void deleteDataMap(String key);
}
