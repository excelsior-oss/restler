package org.restler.http;

import org.restler.client.Call;

/**
 * RequestExecutors are responsible to actually execute HTTP request that will trigger execution on remote server
 * and gather HTTP specific result of call execution (HTTP status code, HTTP headers, response body in case of error etc.).
 * RequestExecutor different from {@code CallExecutor} that it returns not only object, that will be returned to client code,
 * but also HTTP specific information, that can be used to enhance call execution (custom error mapping, authorization expiration
 * detecting etc.).
 */
public interface RequestExecutor {

    Response execute(Call request);

}
