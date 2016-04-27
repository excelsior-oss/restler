package org.restler.spring.data.calls;

import org.restler.client.Call;
import org.restler.client.CallExecutor;
import org.springframework.data.domain.PageImpl;

import java.lang.reflect.Type;
import java.util.List;

public class ConvertToPageCallEnhancer extends CustomCallEnhancer<ConvertToPageCallEnhancer.ConvertToPageCall> {

    public ConvertToPageCallEnhancer() {
        super(ConvertToPageCall.class);
    }

    @Override
    protected Object enhance(ConvertToPageCall call, CallExecutor callExecutor) {
        return new PageImpl<>((List<Object>) callExecutor.execute(call.getCall()));
    }

    public static class ConvertToPageCall implements Call {

        private final Call call;
        private final Type returnType;

        public ConvertToPageCall(Call call, Type returnType) {
            this.call = call;
            this.returnType = returnType;
        }

        @Override
        public Type getReturnType() {
            return returnType;
        }

        @Override
        public Call withReturnType(Type type) {
            return new ConvertToPageCall(call, type);
        }

        public Call getCall() {
            return call;
        }
    }
}
