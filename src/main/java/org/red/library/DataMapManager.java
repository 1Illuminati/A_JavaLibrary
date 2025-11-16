package org.red.library;

import java.util.HashMap;
import java.util.Map;

import org.red.library.serializable.RegisterSerializable;

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
}
