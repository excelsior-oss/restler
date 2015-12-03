package org.restler.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.io.CharStreams;
import com.squareup.okhttp.*;
import org.restler.client.Call;
import org.restler.client.RestlerException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Simple RequestExecutor based on okhttp library designed to be used by custom modules
 */
public class OkHttpRequestExecutor implements RequestExecutor {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient client = new OkHttpClient();

    public OkHttpRequestExecutor(List<Module> jacksonModules) {
        jacksonModules.stream().forEach(objectMapper::registerModule);
    }

    @Override
    public Response execute(Call request) {
        Request okRequest = toOkRequest((HttpCall) request);
        com.squareup.okhttp.Response okResponse = execute(okRequest);
        return toRestlerResponse(okResponse, request.getReturnType());
    }

    private Request toOkRequest(HttpCall request) {
        Request req;
        try {
            Request.Builder requestBuilder = new Request.Builder().
                    method(request.getHttpMethod().toString(), getBody(request)).
                    url(request.getUrl().toString()).
                    headers(toOkHeaders(request.getHeaders()));
            req = requestBuilder.build();
        } catch (JsonProcessingException e) {
            throw new RestlerException("Could not serialize request body", e);
        }
        return req;
    }

    private RequestBody getBody(HttpCall request) throws JsonProcessingException {
        RequestBody body;
        if (request.getHttpMethod() != HttpMethod.GET) {
            body = RequestBody.create(MediaType.parse("application/json"), objectMapper.writeValueAsString(request.getRequestBody()));
        } else {
            body = null;
        }
        return body;
    }

    private Headers toOkHeaders(ImmutableMultimap<String, String> headers) {
        Headers.Builder headersBuilder = new Headers.Builder();
        for (String key : headers.keySet()) {
            for (String header : headers.get(key)) {
                headersBuilder.add(key, header);
            }
        }
        return headersBuilder.build();
    }

    private com.squareup.okhttp.Response execute(Request req) {
        try {
            return client.newCall(req).execute();
        } catch (IOException e) {
            throw new RestlerException("Could not execute request", e);
        }
    }

    private Response toRestlerResponse(com.squareup.okhttp.Response response, Type returnType) {
        HttpStatus status = new HttpStatus(response.code(), response.message());
        InputStream responseBody;
        try {
            responseBody = response.body().byteStream();
            if (response.code() >= 400 && response.code() < 600) {
                return new FailedResponse(status, toString(responseBody), null);
            }
            TypeReference<Object> tr = new RestlerTypeReference(returnType);
            return new SuccessfulResponse(status, toMultiMap(response.headers()), parseResponseBody(response, tr));
        } catch (IOException e) {
            return new FailedResponse(status, null, e);
        }
    }

    private String toString(InputStream responseBody) throws IOException {
        return CharStreams.toString(new InputStreamReader(responseBody));
    }

    private Object parseResponseBody(com.squareup.okhttp.Response response, TypeReference<Object> tr) throws IOException {
        if (tr.getType().getTypeName().equals("java.lang.String")) {
            return response.body().string();
        }
        if (tr.getType().getTypeName().equals("void")) {
            return null;
        } else {
            return objectMapper.readValue(response.body().byteStream(), tr);
        }
    }

    private ImmutableMultimap<String, String> toMultiMap(Headers headers) {
        ImmutableMultimap.Builder<String, String> builder = new ImmutableMultimap.Builder<>();
        headers.toMultimap().keySet().forEach(key -> builder.putAll(key, headers.get(key)));
        return builder.build();
    }

    private static class RestlerTypeReference extends TypeReference<Object> {

        private final Type returnType;

        public RestlerTypeReference(Type returnType) {
            this.returnType = returnType;
        }

        @Override
        public Type getType() {
            return returnType;
        }
    }
}
