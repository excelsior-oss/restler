package org.restler

import org.restler.client.CGLibClientFactory
import org.restler.client.RestlerException
import org.restler.http.security.authentication.CookieAuthenticationStrategy
import org.restler.integration.Controller
import org.restler.spring.mvc.SpringMvcSupport
import org.restler.util.IntegrationSpec
import spock.lang.Specification

import static org.restler.Tests.login
import static org.restler.Tests.password

class SpringMvcIntegrationTest extends Specification implements IntegrationSpec {

    Service serviceWithFormAuth = new Restler("http://localhost:8080", new SpringMvcSupport()).
            formAuthentication(new URI("http://localhost:8080/login"), login, password).
            build();

    Service serviceWithBasicAuth = new Restler("http://localhost:8080", new SpringMvcSupport()).
            httpBasicAuthentication(login, password).
            build();

    def controller = serviceWithFormAuth.produceClient(Controller.class);
    def controllerWithBasicAuth = serviceWithBasicAuth.produceClient(Controller.class)

    def "test unsecured get"() {
        expect:
        "OK" == controller.publicGet()
    }

    def "test get with variable"() {
        expect:
        "Variable OK" == controller.getWithVariable("test", "Variable OK")
    }

    def "test secured get authorized with form auth"() {
        expect:
        "Secure OK" == controller.securedGet()
    }

    def "test secured get authorized with basic auth"() {
        expect:
        "Secure OK" == controllerWithBasicAuth.securedGet()
    }

    def "test exception CGLibClient when class not a controller"() {
        when:
        serviceWithFormAuth.produceClient(CGLibClientFactory.class)
        then:
        thrown(RestlerException)
    }

    def "test exception CookieAuthenticationRequestExecutor when cookie name is empty"() {
        when:
        new CookieAuthenticationStrategy("");
        then:
        thrown(IllegalArgumentException)
    }

    def "methods, that returns void should be called successfully"() {
        when:
        controller.returnVoid()
        then:
        true
    }
}
