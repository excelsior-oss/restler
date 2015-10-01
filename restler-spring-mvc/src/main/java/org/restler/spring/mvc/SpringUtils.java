package org.restler.spring.mvc;

import com.google.common.collect.ImmutableMultimap;
import org.restler.http.HttpCall;
import org.restler.http.HttpForm;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

public final class SpringUtils {

    private SpringUtils() {
    }

    public static <T> HttpCall prepareForSpringMvc(HttpCall call) {
        if (call.getRequestBody() instanceof HttpForm) {
            return new HttpCall(call.getUrl(), call.getHttpMethod(), toMultiValueMap(((HttpForm) call.getRequestBody()).getFields()), call.getHeaders(), call.getReturnType());
        } else {
            return call;
        }
    }

    private static MultiValueMap toMultiValueMap(ImmutableMultimap<String, String> guavaMap) {
        MultiValueMap<String, String> res = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> field : guavaMap.entries()) {
            res.add(field.getKey(), field.getValue());
        }
        return res;
    }

    public static ImmutableMultimap<String, String> toGuavaMultimap(MultiValueMap<String, String> headers) {
        ImmutableMultimap.Builder<String, String> headersBuilder = new ImmutableMultimap.Builder<>();
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            headersBuilder.putAll(header.getKey(), header.getValue());
        }
        return headersBuilder.build();
    }
}
