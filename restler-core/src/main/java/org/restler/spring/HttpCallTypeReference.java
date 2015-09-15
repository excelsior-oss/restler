package org.restler.spring;

import org.restler.client.Call;
import org.springframework.core.ParameterizedTypeReference;

import java.lang.reflect.Type;

class HttpCallTypeReference<T> extends ParameterizedTypeReference<T> {

    private final Call call;

    public HttpCallTypeReference(Call call) {
        this.call = call;
    }

    @Override
    public Type getType() {
        return call.getReturnType();
    }
}
