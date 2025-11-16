package org.red.library.serialize;

import java.io.Serializable;

import org.red.library.DataMap;

/**
 * 직렬화된 DataMap과 직렬화되지 않은 DataMap을 구분하기 위한 클래스
 */
public class SerializeDataMap extends DataMap implements Serializable {
    public static SerializeDataMap convert(DataMap map) {
        return new SerializeDataMap(map);
    }

    public SerializeDataMap() {
        
    }

    private SerializeDataMap(DataMap map) {
        super.getMap().putAll(map.getMap());
    }

    @Override
    public boolean isSerialze() {
        return true;
    }
}
