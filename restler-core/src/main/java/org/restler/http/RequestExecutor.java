package org.restler.http;

import org.restler.client.HttpCall;

public interface RequestExecutor {

    <T> Response<T> execute(HttpCall<T> request);

}
