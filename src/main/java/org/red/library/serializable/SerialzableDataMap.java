package org.red.library.serializable;

import java.io.Serializable;

import org.red.library.DataMap;

public class SerialzableDataMap extends DataMap implements Serializable {
    public static SerialzableDataMap convert(DataMap map) {
        return new SerialzableDataMap(map);
    }

    public SerialzableDataMap() {

    }

    private SerialzableDataMap(DataMap map) {
        super.getMap().putAll(map.getMap());
    }

    @Override
    public boolean isSerialze() {
        return true;
    }
}
