package org.restler

import org.restler.client.CGLibClientFactory
import org.restler.http.RequestExecutionChain
import org.restler.http.security.authentication.CookieAuthenticationStrategy
import org.restler.http.security.authorization.FormAuthorizationStrategy
import org.restler.integration.Controller
import org.restler.spring.RestOperationsRequestExecutor
import org.restler.spring.SpringFormMapper
import org.restler.util.IntegrationSpec
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

class SimpleIntegrationTest extends Specification implements IntegrationSpec {

    def login = "user";
    def password = "password";

    // TODO: find better solution, so users would not required to instantiate
    // TODO: execution chain manually
    def execChain = new RequestExecutionChain(new RestOperationsRequestExecutor(new RestTemplate()), [new SpringFormMapper()])
    def formAuth = new FormAuthorizationStrategy(execChain, new URI("http://localhost:8080/login"), login, "username", password, "password");

    def spySimpleHttpRequestExecutor = Spy(RestOperationsRequestExecutor, constructorArgs: [new RestTemplate()])

    Service serviceWithFormAuth = new ServiceBuilder("http://localhost:8080").
            authorizationStrategy(formAuth).
            cookieBasedAuthentication().
            requestExecutor(spySimpleHttpRequestExecutor).
            build();

    Service serviceWithBasicAuth = new ServiceBuilder("http://localhost:8080").
            httpBasicAuthentication(login, password).
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
