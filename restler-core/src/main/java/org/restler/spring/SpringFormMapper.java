package org.restler.spring;

import org.restler.client.HttpCall;
import org.restler.http.HttpForm;
import org.restler.http.RequestExecutionAdvice;
import org.restler.http.RequestExecutor;
import org.restler.http.Response;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public class SpringFormMapper implements RequestExecutionAdvice {

    private static MultiValueMap toMultiValueMap(HttpForm form) {
        MultiValueMap<String, String> res = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> field : form.getFields().entries()) {
            res.add(field.getKey(), field.getValue());
        }
        return res;
    }

    @Override
    public <T> Response<T> advice(HttpCall<T> call, RequestExecutor requestExecutor) {
        if (call.getRequestBody() instanceof HttpForm) {
            return requestExecutor.execute(new HttpCall<>(call.getUrl(), call.getHttpMethod(), toMultiValueMap((HttpForm) call.getRequestBody()), call.getHeaders(), call.getReturnType()));
        }
        return requestExecutor.execute(call);
    }

}
