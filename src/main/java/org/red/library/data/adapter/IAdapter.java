package org.red.library.data.adapter;

import org.red.library.data.DataMapManager;
import org.red.library.data.serialize.SerializeDataMap;

public interface IAdapter {
    
    SerializeDataMap loadDataMap(String key);

    void saveDataMap(String key, SerializeDataMap value);

    boolean containDataMap(String key);

    void deleteDataMap(String key);
}
