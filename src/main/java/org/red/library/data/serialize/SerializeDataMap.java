package org.red.library.data.serialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.red.library.data.DataMap;

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

    /**
     * 문자열이 { ... } 형태일 때 DataMap으로 복구
     */
    public static SerializeDataMap stringToSerialzableDataMap(String text) {
        text = text.trim();

        // {} 제거
        if (text.startsWith("{") && text.endsWith("}")) {
            text = text.substring(1, text.length() - 1).trim();
        }

        SerializeDataMap map = new SerializeDataMap();

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
    private static Object parseValue(String v) {

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
    private static List<Object> parseList(String text) {
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
    private static List<String> splitTopLevel(String text, char delimiter) {
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
