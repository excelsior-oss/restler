package org.restler.http

import org.restler.spring.mvc.spring.SpringMvcRequestExecutor;
import org.springframework.web.client.RestTemplate

class SpringMvcRequestExecutorTest extends RequestExecutorTest {
    def setup() {
        executor = new SpringMvcRequestExecutor(new RestTemplate())
    }
}
