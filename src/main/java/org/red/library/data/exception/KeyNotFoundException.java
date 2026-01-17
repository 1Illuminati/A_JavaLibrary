package org.red.library.data.exception;

/**
 * 어뎁터에서 데이터를 불러올때 해당 키가 존재하지 않는 데이터 일 경우 반환하는 에러
 */
public class KeyNotFoundException extends Exception {
    public KeyNotFoundException() {
        super("Key is exist");
    }
}
