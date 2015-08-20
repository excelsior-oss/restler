package org.restler

import org.restler.client.CGLibClientFactory
import org.restler.http.RestOperationsRequestExecutor
import org.restler.http.security.authentication.CookieAuthenticationStrategy
import org.restler.http.security.authorization.FormAuthorizationStrategy
import org.restler.integration.Controller
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

class SimpleIntegrationTest extends Specification {

    def login = "user";
    def password = "password";

    def formAuth = new FormAuthorizationStrategy("http://localhost:8080/login", login, "username", password, "password");

    def spySimpleHttpRequestExecutor = Spy(RestOperationsRequestExecutor, constructorArgs: [new RestTemplate()])

    Service serviceWithFormAuth = new ServiceBuilder("http://localhost:8080").
            useAuthorizationStrategy(formAuth).
            useCookieBasedAuthentication().
            useClassNameExceptionMapper().
            useExecutor(spySimpleHttpRequestExecutor).
            build();

    Service serviceWithFormReAuth = new ServiceBuilder("http://localhost:8080").
            useAuthorizationStrategy(formAuth).
            reauthorizeRequestsOnForbidden(true).
            useCookieBasedAuthentication().
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

    def "test deferred get"() {
        def deferredResult = controller.deferredGet()
        def asyncCondition = new AsyncConditions();

        Thread.start {
            while (!deferredResult.hasResult());
            asyncCondition.evaluate {
                assert deferredResult.getResult() == "Deferred OK"
            }
        }

        expect:
        asyncCondition.await(5)
    }

    def "test callable get"() {
        when:
        def result = controller.callableGet()
        def asyncCondition = new AsyncConditions();
        then:
        0 * spySimpleHttpRequestExecutor.execute(_)
        and:
        when:
        Thread.start {
            asyncCondition.evaluate {
                assert result.call() == "Callable OK"
            }
        }
        then:
        asyncCondition.await(5)
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

    def "test reauthorization"() {
        given:
        def ctrl = serviceWithFormReAuth.produceClient(Controller)
        ctrl.logout()
        when:
        def response = ctrl.securedGet()
        then:
        response == "Secure OK"
    }

    def "test exception CGLibClient when class not a controller"() {
        when:
        serviceWithFormAuth.produceClient(CGLibClientFactory.class)
        then:
        thrown(IllegalArgumentException)
    }

    def "test exception CookieAuthenticationRequestExecutor when cookie name is empty"() {
        when:
        new CookieAuthenticationStrategy("");
        then:
        thrown(IllegalArgumentException)
    }

}
