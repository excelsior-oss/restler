package org.restler.http;

public interface RequestExecutor {

    <T> Response<T> execute(Request<T> request);

}
