package org.restler.http.error;

import org.restler.http.HttpExecutionException;
import org.restler.http.Request;
import org.restler.http.RequestExecutionAdvice;
import org.restler.http.RequestExecutor;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassNameErrorMappingRequestExecutionAdvice implements RequestExecutionAdvice {

    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());
    private final Pattern exceptionClassNamePattern = Pattern.compile("(([_\\w]+\\.)+[_\\w]+Exception)");

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void doThrow(Throwable ex) throws T {
        throw (T) ex;
    }

    @Override
    public <T> ResponseEntity<T> advice(Request<T> request, RequestExecutor requestExecutor) {
        try {
            return requestExecutor.execute(request);
        } catch (HttpExecutionException e) {
            Throwable className = findExceptionClassName(e.getResponseBody());
            return doThrow(e, className);
        } catch (HttpServerErrorException e) {
            Throwable className = findExceptionClassName(e.getResponseBodyAsString());
            return doThrow(e, className);
        }
    }

    private <T> ResponseEntity<T> doThrow(RuntimeException e, Throwable ex) {
        if (ex != null) {
            ClassNameErrorMappingRequestExecutionAdvice.<RuntimeException>doThrow(ex);
        }
        throw e;
    }

    private Throwable findExceptionClassName(String responseBody) {
        Matcher matcher = exceptionClassNamePattern.matcher(responseBody);
        while (matcher.find()) {
            String exceptionClassName = null;
            try {
                exceptionClassName = matcher.group(1);
                return (Throwable) Class.forName(exceptionClassName).newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException ignore) {
                log.debug("Could not create instance of " + exceptionClassName);
            }
        }
        return null;
    }
}
