package org.restler.http;

import org.restler.client.Call;

/**
 * RequestExecutors are responsible for actually executing HTTP requests, i.e. they trigger execution on the remote server and gather HTTP-specific results of call execution (HTTP status code, HTTP headers, response body in case of error, etc.).
 * <p>
 * A {@code RequestExecutor} differs from a {@code CallExecutor} in that it returns not just the object that should be returned to client code,
 * but also HTTP-specific information, which can be used to enhance call execution with custom error mapping, authorization expiration detection, and so on.
 * </p>
 */
public interface RequestExecutor {

    Response execute(Call request);

}
