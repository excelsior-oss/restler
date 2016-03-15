package org.restler.api;

import org.restler.client.Call;
import org.restler.client.CallEnhancer;
import org.restler.client.CallExecutor;
import org.restler.http.HttpExecutionException;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThrowExceptionByNameEnhancer implements CallEnhancer {

    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());
    private final Pattern exceptionClassNamePattern = Pattern.compile("(([_\\w]+\\.)+[_\\w]+Exception)");

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void doThrow(Throwable ex) throws T {
        throw (T) ex;
    }

    @Override
    public Object apply(Call call, CallExecutor callExecutor) {
        try {
            return callExecutor.execute(call);
        } catch (HttpExecutionException e) {
            Throwable className = findExceptionClassName(e.getResponseBody().orElse(""));
            return doThrow(e, className);
        }
    }

    private <T> ResponseEntity<T> doThrow(RuntimeException e, Throwable ex) {
        if (ex != null) {
            ThrowExceptionByNameEnhancer.<RuntimeException>doThrow(ex);
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
