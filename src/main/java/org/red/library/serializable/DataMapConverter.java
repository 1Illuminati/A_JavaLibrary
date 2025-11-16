package org.red.library.serializable;

import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;

import org.red.library.DataMap;
import org.red.library.DataMapManager;

public class DataMapConverter {

    private static final String TYPE_KEY = "__type__"; // DataMapSerializable 타입 표시용
    private static final String REGISTER_KEY = "__register__";
    private static final String LIST_KEY = "list";
    private static final String VALUE_KEY = "value";
    private static final String FIELDS_KEY = "fields";
    private static final String CLASS_KEY = "class";
    private static final String MAP_KEY = "map";

    /** 객체를 DataMap으로 직렬화 */
    public DataMap serializeObject(Object obj) {
        if (obj == null) return null;

        SerialzableDataMap result = new SerialzableDataMap();
        Class<?> clazz = obj.getClass();

        if (obj instanceof DataMapSerializable serializable) {
            DataMap map = serializable.serialize();
            for (Map.Entry<String, ?> entry : map.entrySet())
                result.put(entry.getKey(), serializeObject(entry.getValue()));

            result.put(TYPE_KEY, clazz.getName());
        } else if (obj instanceof Map<?, ?> map) {
            Map<String, SerialzableDataMap> data = new HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet())
                data.put(String.valueOf(entry.getKey()), (SerialzableDataMap) serializeObject(entry.getValue()));

            result.put(MAP_KEY, data);
        } else if (obj instanceof Collection) {
            List<Object> list = new ArrayList<>();
            for (Object item : (Collection<?>) obj)
                list.add(serializeObject(item));

            result.put(LIST_KEY, list);
        } else if (isPrimitiveOrWrapper(clazz) || obj instanceof String || obj instanceof UUID) {
            result.put(VALUE_KEY, obj);
        } else if (DataMapManager.containSerializableClass(clazz)) {
            RegisterSerializable serializable = DataMapManager.getSerializableClass(clazz);
            result = SerialzableDataMap.convert(serializable.serialize(obj));
            result.put(REGISTER_KEY, clazz.getName());
        } else {
            DataMap fields = new DataMap();
            Field[] fs = clazz.getDeclaredFields();
            for (Field f : fs) {
                f.setAccessible(true);
                try {
                    fields.put(f.getName(), serializeObject(f.get(obj)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            result.put(CLASS_KEY, clazz.getName());
            result.put(FIELDS_KEY, fields);
        }

        return result;
    }

    public <T> T deserializeObject(DataMap data, Class<T> clazz) {
        return clazz.cast(deserializeObject(data));
    }

    /** DataMap -> 객체 역직렬화 (clazz 인자 없음) */
    public Object deserializeObject(DataMap data) {
        System.out.println(data);
        if (data == null) return null;
        else if (!data.isSerialze())
            throw new IllegalArgumentException("잘못된 DataMap, isSerialize가 true인 dataMap만 deserialize가 가능합니다");

        // 1. DataMapSerializable 객체
        if (data.containsKey(TYPE_KEY)) {
            System.out.println("1" + data);
            String className = data.getString(TYPE_KEY);
            data.remove(TYPE_KEY);

            DataMap m = new DataMap();
            for (Map.Entry<String, Object> entry : data.entrySet())
                m.put(entry.getKey(), deserializeObject((DataMap) entry.getValue()));

            try {
                Class<?> cls = Class.forName(className);
                Method method = cls.getDeclaredMethod("deserialize", DataMap.class);
                return method.invoke(null, m);
            } catch (Exception e) {
            }
        }

        if (data.containsKey(REGISTER_KEY)) {
            String className = data.getString(REGISTER_KEY);
            data.remove(REGISTER_KEY);
            try {
                Class<?> cls = Class.forName(className);
                if (!DataMapManager.containSerializableClass(cls)) throw new IllegalStateException("미등록 상황 " + className + " 의 RegisterSerializable를 등록해주세요");
                RegisterSerializable regSerializable = DataMapManager.getSerializableClass(cls);

                return regSerializable.deserialize(data);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (data.containsKey(MAP_KEY)) {
            SerialzableDataMap map = data.getClass(MAP_KEY, SerialzableDataMap.class);
            Map result = new HashMap<>();
            for (Entry<String, Object> entry : map.entrySet())
                result.put(entry.getKey(), (deserializeObject((SerialzableDataMap) entry.getValue())));

            return result;
        }

        // 2. List
        if (data.containsKey(LIST_KEY)) {
            List<Object> list = new ArrayList<>();
            List<?> rawList = data.getList(LIST_KEY, new ArrayList<>());
            for (Object item : rawList) {
                if (item instanceof DataMap) {
                    list.add(deserializeObject((SerialzableDataMap) item));
                } else {
                    list.add(item);
                }
            }
            return list;
        }

        // 3. 기본 타입
        if (data.containsKey(VALUE_KEY)) {
            return data.get(VALUE_KEY);
        }

        // 4. 일반 객체
        if (data.containsKey(FIELDS_KEY) && data.containsKey(CLASS_KEY)) {
            String className = data.getString(CLASS_KEY);
            try {
                Class<?> clazz = Class.forName(className);
                Object instance = clazz.getDeclaredConstructor().newInstance();
                DataMap fields = data.getDataMap(FIELDS_KEY);
                for (String key : fields.keySet()) {
                    try {
                        Field field = clazz.getDeclaredField(key);
                        field.setAccessible(true);
                        Object val = fields.get(key);
                        if (val instanceof DataMap) {
                            field.set(instance, deserializeObject((SerialzableDataMap) val));
                        } else {
                            field.set(instance, val);
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                return instance;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // 5. 나머지 경우: 그냥 DataMap 그대로 반환
        return data;
    }

    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == Integer.class
                || clazz == Double.class
                || clazz == Float.class
                || clazz == Long.class
                || clazz == Short.class
                || clazz == Byte.class
                || clazz == Boolean.class
                || clazz == Character.class;
    }

    /**
     * 문자열이 { ... } 형태일 때 DataMap으로 복구
     */
    public SerialzableDataMap stringToSerialzableDataMap(String text) {
        text = text.trim();

        // {} 제거
        if (text.startsWith("{") && text.endsWith("}")) {
            text = text.substring(1, text.length() - 1).trim();
        }

        SerialzableDataMap map = new SerialzableDataMap();

        // key=value 형태의 덩어리를 분리
        List<String> tokens = splitTopLevel(text, ',');

        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

            int eq = token.indexOf('=');
            if (eq == -1) continue;

            String key = token.substring(0, eq).trim();
            String valueStr = token.substring(eq + 1).trim();

            Object value = parseValue(valueStr);

            map.put(key, value);
        }

        return map;
    }

    private Map parseMap(String text) {
        text = text.trim();

        // {} 제거
        if (text.startsWith("{") && text.endsWith("}")) {
            text = text.substring(1, text.length() - 1).trim();
        }

        Map map = new HashMap<>();

        // key=value 형태의 덩어리를 분리
        List<String> tokens = splitTopLevel(text, ',');

        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

            int eq = token.indexOf('=');
            if (eq == -1) continue;

            String key = token.substring(0, eq).trim();
            String valueStr = token.substring(eq + 1).trim();

            Object value = parseValue(valueStr);

            map.put(key, value);
        }

        return map;
    }

    /**
     * value가 어떤 타입인지 판단하여 파싱
     */
    private Object parseValue(String v) {

        // 리스트 [ ... ]
        if (v.startsWith("[") && v.endsWith("]")) {
            return parseList(v);
        }

        // Map { ... }
        if (v.startsWith("{") && v.endsWith("}")) {
            return stringToSerialzableDataMap(v);
        }

        // 숫자인지 체크
        if (isInteger(v)) return Integer.parseInt(v);
        if (isDouble(v)) return Double.parseDouble(v);

        // 문자열로 처리 (따옴표 없어도 문자열로 본다)
        return v;
    }

    /**
     * [a, b, 1, 3.14] 형태의 문자열을 List<Object>로 재구성
     */
    private List<Object> parseList(String text) {
        text = text.trim();
        text = text.substring(1, text.length() - 1).trim(); // [] 제거

        List<String> tokens = splitTopLevel(text, ',');

        List<Object> list = new ArrayList<>();
        for (String t : tokens) {
            t = t.trim();
            if (t.isEmpty()) continue;
            list.add(parseValue(t));
        }
        return list;
    }


    /**
     * 최상위 레벨에서만 split하는 유틸
     * 예:
     *  map: {a=10, b={x=1, y=2}, c=[1,2,3]}
     *  splitTopLevel(text, ',') → ["a=10", "b={x=1, y=2}", "c=[1,2,3]"]
     */
    private List<String> splitTopLevel(String text, char delimiter) {
        List<String> result = new ArrayList<>();

        int depth = 0;
        StringBuilder sb = new StringBuilder();

        for (char c : text.toCharArray()) {

            // 깊이 계산
            if (c == '{' || c == '[') depth++;
            if (c == '}' || c == ']') depth--;

            // 현재 depth 0에서 delimiter이면 split
            if (depth == 0 && c == delimiter) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        if (sb.length() > 0) result.add(sb.toString());

        return result;
    }


    // 숫자 판별 유틸
    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e) { return false; }
    }

    private static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return s.contains(".");
        } catch (Exception e) { return false; }
    }
}
