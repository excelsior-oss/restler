package org.restler.itest

import org.restler.Service
import org.restler.ServiceBuilder
import org.restler.client.CGLibClientFactory
import org.restler.http.HttpExecutionException
import org.restler.http.RestOperationsExecutor
import org.restler.http.security.authentication.CookieAuthenticationStrategy
import org.restler.http.security.authorization.FormAuthorizationStrategy
import org.restler.testserver.Controller
import org.restler.util.Util
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

import java.util.concurrent.Executors

class SimpleTest extends Specification {

    def login = "user";
    def password = "password";

    def formAuth = new FormAuthorizationStrategy("http://localhost:8080/login", login, "username", password, "password");

    Service serviceWithFormAuth = new ServiceBuilder("http://localhost:8080").
            useAuthorizationStrategy(formAuth).
            useCookieBasedAuthentication().
            reauthorizeRequestsOnForbidden().
            useThreadExecutor(Executors.newCachedThreadPool()).
            useClassNameExceptionMapper().
            useRequestExecutor(new RestOperationsExecutor(new RestTemplate())).
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
        def result = controller.callableGet()
        def asyncCondition = new AsyncConditions();

        Thread.start {
            asyncCondition.evaluate {
                assert result.call() == "Callable OK"
            }
        }

        expect:
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

    def "test utils toString"() {
        expect:
        String testString = "Util toString OK"
        InputStream inputStream = new ByteArrayInputStream(testString.getBytes("UTF-8"))
        Util.toString(inputStream) == testString
    }

    def "test exception CGLibClient when class not a controller"() {
        when:
        serviceWithFormAuth.produceClient(CGLibClientFactory.class)
        then:
        thrown(IllegalArgumentException)
    }

    def "test exception CookieAuthenticationRequestExecutor when cookie name is empty"() {
        when:
        new CookieAuthenticationStrategy("", null, null);
        then:
        thrown(IllegalArgumentException)
    }
}
