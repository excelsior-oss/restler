package org.restler.http.security;

import org.restler.http.ExecutionAdvice;
import org.restler.http.Executor;
import org.restler.http.Request;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

public class ReauthorizingExecutionAdvice implements ExecutionAdvice {

    private final SecuritySession session;

    public ReauthorizingExecutionAdvice(SecuritySession session) {
        this.session = session;
    }

    @Override
    public <T> ResponseEntity<T> advice(Request<T> request, Executor executor) {
        ResponseEntity<T> response;
        try {
            response = executor.execute(request);
        } catch (HttpClientErrorException e) {
            response = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (response.getStatusCode().value() == HttpStatus.FORBIDDEN.value()) {
            session.authorize();
            return executor.execute(request);
        }

        return response;
    }
}
