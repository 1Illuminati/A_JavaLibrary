package org.red.library.data.serialize;

import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;

import org.red.library.data.DataMap;
import org.red.library.data.DataMapManager;

/**
 * 데이터를 DataMap으로 직렬화 하는 클래스 DataMap또한 직렬화 시켜야한다.
 * String변환은 toString으로 String -> SerializeDataMap기능또한 지원한다.
 */
public class DataMapConverter {

    private static final String TYPE_KEY = "__type__"; // DataMapSerializable 타입 표시용
    private static final String REGISTER_KEY = "__register__";
    private static final String LIST_KEY = "list";
    private static final String VALUE_KEY = "value";
    private static final String FIELDS_KEY = "fields";
    private static final String CLASS_KEY = "class";
    private static final String MAP_KEY = "map";
    private final DataMapManager manager;

    public DataMapConverter(DataMapManager manager) {
        this.manager = manager;
    }

    /** 
     * 객체를 DataMap으로 직렬화 
     */
    public SerializeDataMap serializeObject(Object obj) {
        if (obj == null) return null;

        else if (obj instanceof DataMap m && m.isSerialze())
            throw new IllegalArgumentException("잘못된 DataMap, isSerialize가 false인 dataMap만 serialize가 가능합니다");

        SerializeDataMap result = new SerializeDataMap();
        Class<?> clazz = obj.getClass();

        if (obj instanceof DataMapSerializable serializable) {
            DataMap map = serializable.serialize();
            for (Map.Entry<String, ?> entry : map.entrySet())
                result.put(entry.getKey(), serializeObject(entry.getValue()));

            result.put(TYPE_KEY, clazz.getName());
        } else if (obj instanceof Map<?, ?> map) {
            Map<String, SerializeDataMap> data = new HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet())
                data.put(String.valueOf(entry.getKey()), (SerializeDataMap) serializeObject(entry.getValue()));

            result.put(MAP_KEY, data);
        } else if (obj instanceof Collection) {
            List<Object> list = new ArrayList<>();
            for (Object item : (Collection<?>) obj)
                list.add(serializeObject(item));

            result.put(LIST_KEY, list);
        } else if (isPrimitiveOrWrapper(clazz) || obj instanceof String || obj instanceof UUID) {
            result.put(VALUE_KEY, obj);
        } else if (manager.containSerializableClass(clazz)) {
            RegisterSerializable serializable = manager.getSerializableClass(clazz);
            result = SerializeDataMap.convert(serializable.serialize(obj));
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

    /**
     * 클래스로 캐스트해서 불러오기 편하게 할때 쓰는 함수
     */
    public <T> T deserializeObject(SerializeDataMap data, Class<T> clazz) {
        return clazz.cast(deserializeObject(data));
    }

    /** 
     * DataMap -> 객체 역직렬화 함수
     */
    public Object deserializeObject(SerializeDataMap data) {
        if (data == null) return null;
        else if (!data.isSerialze())
            throw new IllegalArgumentException("잘못된 DataMap, isSerialize가 true인 dataMap만 deserialize가 가능합니다");

        // 1. DataMapSerializable 객체
        if (data.containsKey(TYPE_KEY)) {
            String className = data.getString(TYPE_KEY);
            data.remove(TYPE_KEY);

            DataMap m = new DataMap();
            for (Map.Entry<String, Object> entry : data.entrySet())
                m.put(entry.getKey(), deserializeObject((SerializeDataMap) entry.getValue()));

            try {
                Class<?> cls = Class.forName(className);
                Method method = cls.getDeclaredMethod("deserialize", DataMap.class);
                return method.invoke(null, m);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (data.containsKey(REGISTER_KEY)) {
            String className = data.getString(REGISTER_KEY);
            data.remove(REGISTER_KEY);
            try {
                Class<?> cls = Class.forName(className);
                if (!manager.containSerializableClass(cls)) throw new IllegalStateException("미등록 상황 " + className + " 의 RegisterSerializable를 등록해주세요");
                RegisterSerializable regSerializable = manager.getSerializableClass(cls);

                return regSerializable.deserialize(data);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (data.containsKey(MAP_KEY)) {
            SerializeDataMap map = data.getClass(MAP_KEY, SerializeDataMap.class);
            Map result = new HashMap<>();
            for (Entry<String, Object> entry : map.entrySet())
                result.put(entry.getKey(), (deserializeObject((SerializeDataMap) entry.getValue())));

            return result;
        }

        // 2. List
        if (data.containsKey(LIST_KEY)) {
            List<Object> list = new ArrayList<>();
            List<?> rawList = data.getList(LIST_KEY, new ArrayList<>());
            for (Object item : rawList) {
                if (item instanceof DataMap) {
                    list.add(deserializeObject((SerializeDataMap) item));
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
                            field.set(instance, deserializeObject((SerializeDataMap) val));
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

    /**
     * 지원하는 클래스인지 감지 함수
     */
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
}
