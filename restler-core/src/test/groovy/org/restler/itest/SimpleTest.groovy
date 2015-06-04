package org.restler.itest

import org.restler.Service
import org.restler.ServiceBuilder
import org.restler.http.security.authorization.FormAuthorizationStrategy
import org.restler.testserver.Controller
import spock.lang.Specification

class SimpleTest extends Specification {

    def auth = new FormAuthorizationStrategy("http://localhost:8080/login", "user", "username", "password", "password")
    Service service = new ServiceBuilder("http://localhost:8080").
            useAuthorizationStrategy(auth).
            useCookieBasedAuthentication().
            reauthorizeRequestsOnForbidden().
            build();

    def controller = service.produceClient(Controller.class);

    def "test unsecured get"() {
        expect:
        "OK" == controller.publicGet()
    }

    def "test secured get authorized"() {
        when:
        service.authorize();
        then:
        "Secure OK" == controller.securedGet()
    }

    def "test reauthorization"() {
        given:
        service.authorize()
        def ctrl = service.produceClient(Controller)
        ctrl.logout()
        when:
        def response = ctrl.securedGet()
        then:
        response == "Secure OK"
    }

}
