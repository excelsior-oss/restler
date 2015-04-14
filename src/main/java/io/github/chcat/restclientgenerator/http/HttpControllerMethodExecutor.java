package io.github.chcat.restclientgenerator.http;

import java.util.Map;

import javax.xml.ws.Response;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.github.chcat.restclientgenerator.ControllerMethodExecutor;

/**
 * Created by pasa on 19.02.2015.
 */
public class HttpControllerMethodExecutor implements ControllerMethodExecutor {


    @Override
    public Object execute(RequestMapping controllerMapping, RequestMapping methodMapping, RequestMethod requestType, ResponseStatus expectedStatus, Object requestBody, ResponseBody methodBodyAnnotation, Class<?> returnType, Map<String, Object> pathVariables, Map<String, Object> requestParams) {

        RequestMethod callType;
        if (methodMapping.method() == null || methodMapping.method().length == 0) {
            callType = RequestMethod.GET;
        } else {
            callType = methodMapping.method()[0];
        }

        if (requestBody != null && (callType == RequestMethod.POST || callType == RequestMethod.PUT)) {
            specification = specification.body(requestBody).contentType(ContentType.JSON);
        }

        specification = specification.header("Host", RestAssured.baseURI.substring(RestAssured.baseURI.indexOf("//") + 2));

        Response response;

        switch (callType) {
            case POST:
                response = specification.post(uriTemplate);
                break;
            case PUT:
                response = specification.put(uriTemplate);
                break;
            case GET:
                response = specification.get(uriTemplate);
                break;
            case DELETE:
                response = specification.delete(uriTemplate);
                break;

            default:
                throw new AssertionError();

        }

        String responseString = response.getBody().asString();

        ensureExceptedStatus(response, responseString, expectedStatus(method));

        Class resultType = method.getReturnType();

        if (String.class.equals(resultType)) {
            return responseString;
        } else if (Boolean.class.equals(resultType) || boolean.class.equals(resultType)) {
            return Boolean.valueOf(responseString);
        } else if (Void.TYPE.equals(resultType) || RedirectView.class.equals(resultType)) {
            return null;
        } else {
            return JsonPath.from(responseString).getObject("", resultType);
        }

        return null;
    }
}
