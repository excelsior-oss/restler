package org.restler.spring;

import org.restler.http.*;
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

    @Override public <T> Response<T> advice(Request<T> request, RequestExecutor requestExecutor) {
        if (request.getBody() instanceof HttpForm) {
            return requestExecutor.execute(new Request<>(request.getUrl(), request.getHttpMethod(), request.getHeaders(), toMultiValueMap((HttpForm) request.getBody()), request.getReturnType()));
        }
        return requestExecutor.execute(request);
    }
}
