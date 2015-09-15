package org.restler.client;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbstractWrapperHandler<T> implements CallExecutionAdvice<Object> {

    @Override
    public Object advice(Call call, CallExecutor callExecutor) {
        TypeToken<?> type = TypeToken.of(call.getReturnType());
        if (type.getRawType().equals(wrapperClass())) {
            ParameterizedType parameterizedType = (ParameterizedType) call.getReturnType();
            Type actualReturnType = parameterizedType.getActualTypeArguments()[0];
            Call actualCall = call.withReturnType(actualReturnType);
            return execute(callExecutor, actualCall);
        } else {
            return callExecutor.execute(call);
        }
    }

    protected abstract Class<?> wrapperClass();

    protected abstract T execute(CallExecutor callExecutor, Call actualCall);

}
