package org.restler.spring.data.util;

import org.springframework.data.domain.PageImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class PageType implements ParameterizedType {

    private final Type itemType;

    public PageType(Type itemType) {
        this.itemType = itemType;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{itemType};
    }

    @Override
    public Type getRawType() {
        return PageImpl.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}
