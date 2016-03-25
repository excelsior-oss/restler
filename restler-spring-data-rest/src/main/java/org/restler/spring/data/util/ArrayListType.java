package org.restler.spring.data.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class ArrayListType implements ParameterizedType {

    private final Type itemType;

    public ArrayListType(Type itemType) {
        this.itemType = itemType;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[] {itemType};
    }

    @Override
    public Type getRawType() {
        return ArrayList.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}