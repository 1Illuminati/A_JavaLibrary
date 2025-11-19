package org.red.library.data.serialize;

import org.red.library.data.DataMap;

/**
 * 이미 만들어져 있어서 수정이 불가능한 클래스를 등록하여 사용할때 사용
 * 원본 클래스 및 해당 클래스를 생성하여 등록한다
 */
public interface RegisterSerializable<T> {

    DataMap serialize(T origin);

    T deserialize(DataMap map);
}
