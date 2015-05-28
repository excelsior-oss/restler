package org.restler.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/**
 * Describes a mapped controller method.
 * @param <T> the return type of the method
 */
public class MappedMethodDescription<T>{
    private String uriTemplate;
    private Class<T> returnType;
    private HttpMethod httpMethod;
    private HttpStatus expectedHttpResponseStatus;

    public MappedMethodDescription(String uriTemplate, Class<T> returnType, HttpMethod httpMethod, HttpStatus expectedHttpResponseStatus) {
        this.uriTemplate = uriTemplate;
        this.returnType = returnType;
        this.httpMethod = httpMethod;
        this.expectedHttpResponseStatus = expectedHttpResponseStatus;
    }

    /**
     * Provides a URI template the method is mapped onto.
     * @return a URI template string
     */
    public String getUriTemplate() {
        return uriTemplate;
    }

    /**
     * Provides the return type of the method
     * @return a type object
     */
    public Class<T> getReturnType() {
        return returnType;
    }

    /**
     * Provides the {@link HttpMethod} associated with the method
     * @return an HTTP method
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public HttpStatus getExpectedHttpResponseStatus() {
        return expectedHttpResponseStatus;
    }
}
