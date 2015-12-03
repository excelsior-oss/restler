package org.restler.http;

class OkHttpRequestExecutorTest extends RequestExecutorTest {
    def setup() {
        executor = new OkHttpRequestExecutor([])
    }
}
