package org.restler.spring.mvc.spring;

import com.google.common.collect.ImmutableMultimap;
import org.restler.http.HttpCall;
import org.restler.http.HttpForm;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

public class SpringUtils {

    private static boolean alreadyChecked = false;
    private static boolean isSpringAvailableFlag = false;

    public static boolean isSpringAvailable() {

        if(!alreadyChecked) {
            isSpringAvailableFlag = checkSpring();
            alreadyChecked = true;
        }

        return isSpringAvailableFlag;
    }

    public static <T> HttpCall prepareForSpringMvc(HttpCall call) {
        if (call.getRequestBody() instanceof HttpForm) {
            return new HttpCall(call.getUrl(), call.getHttpMethod(), ((HttpForm) call.getRequestBody()).getFields(), call.getHeaders(), call.getReturnType());
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

    private static boolean checkSpring() {
        try {
            Class.forName("org.springframework.web.context.request.async.DeferredResult");
            Class.forName("org.springframework.web.client.RestTemplate");
            Class.forName("org.springframework.http.converter.json.MappingJackson2HttpMessageConverter");

            Class.forName("org.springframework.util.LinkedMultiValueMap");
            Class.forName("org.springframework.util.MultiValueMap");

            Class.forName("org.springframework.http.HttpHeaders");
            Class.forName("org.springframework.http.HttpMethod");
            Class.forName("org.springframework.http.RequestEntity");
            Class.forName("org.springframework.http.ResponseEntity");
            Class.forName("org.springframework.http.converter.HttpMessageNotWritableException");
            Class.forName("org.springframework.util.MultiValueMap");
            Class.forName("org.springframework.web.client.HttpStatusCodeException");
            Class.forName("org.springframework.web.client.RestTemplate");

            Class.forName("org.springframework.core.ParameterizedTypeReference");
        }
        catch (ClassNotFoundException e) {
            return false;
        }

        return true;
    }
}
