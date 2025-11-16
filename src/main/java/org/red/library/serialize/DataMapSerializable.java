package org.red.library.serialize;

import org.red.library.DataMap;

/**
 * DataMap으로 저장이 가능한 클래스를 만들때 implements하는 클래스
 * static 함수로 deserialize를 만들어줘야만 한다
 */
public interface DataMapSerializable {
    DataMap serialize();
}
