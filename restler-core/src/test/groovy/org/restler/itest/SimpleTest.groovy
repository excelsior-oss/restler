package org.restler.itest

import org.restler.Service
import org.restler.ServiceBuilder
import org.restler.http.security.authorization.FormAuthorizationStrategy
import org.restler.testserver.Controller
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

import java.util.concurrent.Executors
import java.util.function.Supplier

class SimpleTest extends Specification {

    def login = "user";
    def password = "password";

    def formAuth = new FormAuthorizationStrategy("http://localhost:8080/login", login, "username", password, "password");

    def mockThreadExecutor = Mock(Supplier)
    def threadExecutorSupplier = Executors.newCachedThreadPool()

    Service serviceWithFormAuth = new ServiceBuilder("http://localhost:8080").
            useAuthorizationStrategy(formAuth).
            useCookieBasedAuthentication().
            reauthorizeRequestsOnForbidden().
            useThreadExecutorSupplier(mockThreadExecutor).
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

    def setup() {
        mockThreadExecutor.get() >> threadExecutorSupplier
    }

    def "test unsecured get"() {
        expect:
        "OK" == controller.publicGet()
    }

    def "test count calling thread executor supplier"() {
        when:
        controller.publicGet()
        then:
        0 * mockThreadExecutor.get() >> threadExecutorSupplier
        and:
        when:
        for (def i : 0..1) {
            controller.deferredGet()
        }
        then:
        1 * mockThreadExecutor.get() >> threadExecutorSupplier
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

}
