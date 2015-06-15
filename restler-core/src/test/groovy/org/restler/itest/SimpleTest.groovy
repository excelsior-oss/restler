package org.restler.itest

import org.restler.Service
import org.restler.ServiceBuilder
import org.restler.http.security.authorization.FormAuthorizationStrategy
import org.restler.testserver.Controller
import spock.lang.Specification

class SimpleTest extends Specification {

    def login = "user";
    def password = "password";

    def formAuth = new FormAuthorizationStrategy("http://localhost:8080/login", login, "username", password, "password");

    Service serviceWithFormAuth = new ServiceBuilder("http://localhost:8080").
            useAuthorizationStrategy(formAuth).
            useCookieBasedAuthentication().
            reauthorizeRequestsOnForbidden().
            build();

    Service serviceWithBasicAuth = new ServiceBuilder("http://localhost:8080").
            useHttpBasicAuthentication(login, password).
            build();

    def controller = serviceWithFormAuth.produceClient(Controller.class);
    def controllerWithBasicAuth = serviceWithBasicAuth.produceClient(Controller.class);

    def "test unsecured get"() {
        expect:
        "OK" == controller.publicGet()
    }

    def "test secured get authorized with form auth"() {
        when:
        serviceWithFormAuth.authorize();
        then:
        "Secure OK" == controller.securedGet()
    }

    def "test secured get authorized with basic auth"() {
        when:
        serviceWithBasicAuth.authorize();
        then:
        "Secure OK" == controllerWithBasicAuth.securedGet()
    }

    def "test reauthorization"() {
        given:
        serviceWithFormAuth.authorize()
        def ctrl = serviceWithFormAuth.produceClient(Controller)
        ctrl.logout()
        when:
        def response = ctrl.securedGet()
        then:
        response == "Secure OK"
    }

}
