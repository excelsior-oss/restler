package org.restler.http

import org.restler.spring.data.SpringDataRequestExecutor
import org.springframework.web.client.RestTemplate

class SpringDataRequestExecutorTest extends RequestExecutorTest {
    def setup() {
        executor = new SpringDataRequestExecutor(new RestTemplate())
    }
}
