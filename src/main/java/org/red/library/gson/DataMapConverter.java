package org.red.library.gson;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.red.library.DataMap;
import org.red.library.serializable.DataMapSerializable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * DataMap을 json으로 convert후 사용하기 위하여 제작
 */
public class DataMapConverter {
    public JsonObject convert(DataMap map) {
        JsonObject data = new JsonObject();

        for (Entry<String, ?> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Class<?> clazz = value.getClass();

            if (isJsonSupported(clazz)) data.add(key, toJsonElement(value));
            else if (value instanceof DataMapSerializable serializaeClass) {
                JsonObject obj = new JsonObject();
                obj.addProperty("class", clazz.getName());
                obj.add("data", convert(serializaeClass.serialize()));
                data.add(key, obj);
            }
            else if (DataMap.containSerializableClass(clazz)) {
                JsonObject obj = new JsonObject();
                obj.addProperty("class", clazz.getName());
                obj.add("data", convert(DataMap.getSerializableClass(clazz).serialize(value)));
                data.add(key, obj);
            }
        }

        return data;
    }

    public DataMap convert(JsonObject obj) {
        DataMap map = new DataMap();

        for (Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (value.isJsonPrimitive()) {
                JsonPrimitive prim = value.getAsJsonPrimitive();
                if (prim.isBoolean()) {
                    map.put(key, prim.getAsBoolean());
                } else if (prim.isNumber()) {
                    map.put(key, prim.getAsNumber());
                } else if (prim.isString()) {
                    map.put(key, prim.getAsString());
                }
            }
        }


        return map;
    }

    public JsonElement toJsonElement(Object obj) {
        if (obj == null) {
            return JsonNull.INSTANCE;
        }

        // 이미 JsonElement이면 그대로 반환
        if (obj instanceof JsonElement) {
            return (JsonElement) obj;
        }

        // JsonPrimitive로 변환 가능한 타입
        if (obj instanceof Boolean) {
            return new JsonPrimitive((Boolean) obj);
        } else if (obj instanceof Number) {
            return new JsonPrimitive((Number) obj);
        } else if (obj instanceof Character) {
            return new JsonPrimitive((Character) obj);
        } else if (obj instanceof String) {
            return new JsonPrimitive((String) obj);
        }

        // Map<String, ?> → JsonObject
        if (obj instanceof Map<?, ?> mapObj) {
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<?, ?> entry : mapObj.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (!(key instanceof String)) {
                    throw new IllegalArgumentException("Map key must be String: " + key);
                }
                jsonObject.add((String) key, toJsonElement(value));
            }
            return jsonObject;
        }

        // Collection<?> → JsonArray
        if (obj instanceof Collection<?> collection) {
            JsonArray jsonArray = new JsonArray();
            for (Object item : collection) {
                jsonArray.add(toJsonElement(item));
            }
            return jsonArray;
        }

        // 배열 → JsonArray
        if (obj.getClass().isArray()) {
            JsonArray jsonArray = new JsonArray();
            int length = java.lang.reflect.Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                Object item = java.lang.reflect.Array.get(obj, i);
                jsonArray.add(toJsonElement(item));
            }
            return jsonArray;
        }

        // 지원하지 않는 타입
        throw new IllegalArgumentException("지원하지 않는 타입: " + obj.getClass());
    }


    /*
    * JsonObject(Json-P / javax.json)로 처리 가능한 타입인지 감지하는 함수
    * JsonObject는 다음 타입만 지원:
    *  - String
    *  - Number(Integer, Long, Double, Float 등)
    *  - Boolean
    *  - JsonObject / JsonArray
    *  - Map<String, ?>   → JsonObject로 변환 가능
    *  - Collection<?>    → JsonArray로 변환 가능
    *  - 배열(Object[])   → JsonArray로 변환 가능
    *
    * 아래 함수는 위 기준을 충족하면 true, 아니면 false 반환
    */
    public boolean isJsonSupported(Class<?> clazz) {

        // null 타입은 처리 불가
        if (clazz == null)
            return false;

        // 문자열
        if (clazz == String.class)
            return true;

        // 숫자(Number 계열: Integer, Long, Double, Float, Short 등)
        if (Number.class.isAssignableFrom(clazz) ||
                clazz.isPrimitive() && (clazz == int.class ||
                                        clazz == long.class ||
                                        clazz == double.class ||
                                        clazz == float.class ||
                                        clazz == short.class ||
                                        clazz == byte.class))
            return true;

        // boolean / Boolean
        if (clazz == boolean.class || clazz == Boolean.class)
            return true;

        // Map<String, ?> 형태는 JsonObject가 지원
        if (Map.class.isAssignableFrom(clazz))
            return true;

        // List, Set 등 Collection은 JsonArray로 변환 가능
        if (Collection.class.isAssignableFrom(clazz))
            return true;

        // 배열(Object[])도 JsonArray로 변환 가능
        if (clazz.isArray())
            return true;

        // 그 외는 Json-P에서 직접 변환 불가능한 타입
        return false;
    }
}
