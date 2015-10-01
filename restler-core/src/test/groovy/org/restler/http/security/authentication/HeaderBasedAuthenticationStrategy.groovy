package org.restler.http.security.authentication

import org.restler.http.Header
import org.restler.http.HttpCall
import org.restler.http.HttpMethod
import spock.lang.Specification

class HeaderBasedAuthenticationStrategySpec extends Specification {

    def "HeaderBasedAuthenticationStrategy should support multiple headers"() {
        given:
        def auth = new TestAuthStrategy()
        def req = new HttpCall(URI.create("http://localhost"), HttpMethod.GET, null)

        when:
        def authReq = auth.authenticate(req, null)

        then:
        authReq.getHeaders().get("name").first() == "test-name"
        authReq.getHeaders().get("pass").first() == "test-pass"
    }

}

class TestAuthStrategy extends HeaderBasedAuthenticationStrategy {

    @Override
    protected List<Header> headers(AuthenticationContext context) {
        return [new Header("name", "test-name"), new Header("pass", "test-pass")]
    }

}
