package org.restler.http.security.authentication

import org.restler.http.Request
import org.springframework.http.HttpMethod
import spock.lang.Specification

class HeaderBasedAuthenticationStrategySpec extends Specification {

    def "HeaderBasedAuthenticationStrategy should support multiple headers"() {
        given:
        def auth = new TestAuthStrategy()
        def req = new Request(URI.create("http://localhost"), HttpMethod.GET, null, null)

        when:
        def authReq = auth.authenticate(req, null)

        then:
        authReq.toRequestEntity().getHeaders().getFirst("name") == "test-name"
        authReq.toRequestEntity().getHeaders().getFirst("pass") == "test-pass"
    }

}

class TestAuthStrategy extends HeaderBasedAuthenticationStrategy {

    @Override
    protected List<Header> headers(AuthenticationContext context) {
        return [new Header("name", "test-name"), new Header("pass", "test-pass")]
    }

}
