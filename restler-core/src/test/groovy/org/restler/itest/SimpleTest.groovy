package org.restler.itest
import org.restler.Service
import org.restler.ServiceBuilder
import org.restler.http.security.authentication.CookieBasedAuthenticationStrategy
import org.restler.http.security.authorization.FormAuthorizationStrategy
import org.restler.testserver.Controller
import spock.lang.Specification

class SimpleTest extends Specification {

    Service service = (new ServiceBuilder("http://localhost:8080")).useAuthenticationStrategy(new CookieBasedAuthenticationStrategy()).build();

    def controller = service.client(Controller.class);

    def "test unsecured get" () {
        expect:
            "OK" == controller.publicGet()
    }

    def "test secured get authorized" () {
        given:
            def auth = new FormAuthorizationStrategy("http://localhost:8080/login","user","username", "password","password")
        when:
            service.authorize(auth);
        then:
            "Secure OK" == controller.securedGet()
    }
    
}
