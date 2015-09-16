package org.restler

import org.restler.client.CGLibClientFactory
import org.restler.client.RestlerException
import org.restler.http.security.authentication.CookieAuthenticationStrategy
import org.restler.http.security.authorization.FormAuthorizationStrategy
import org.restler.integration.Controller
import org.restler.spring.SpringMvcRequestExecutor
import org.restler.spring.SpringMvcSupport
import org.restler.util.IntegrationSpec
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

class SimpleIntegrationTest extends Specification implements IntegrationSpec {

    def login = "user";
    def password = "password";

    // TODO: find better solution, so users would not required to instantiate execution chain manually
    def executor = new SpringMvcRequestExecutor(new RestTemplate())
    def formAuth = new FormAuthorizationStrategy(executor, new URI("http://localhost:8080/login"), login, "username", password, "password");

    def spySimpleHttpRequestExecutor = Spy(SpringMvcRequestExecutor, constructorArgs: [new RestTemplate()])


    SpringMvcSupport support = new SpringMvcSupport().
            requestExecutor(spySimpleHttpRequestExecutor)

    Service serviceWithFormAuth = new Restler("http://localhost:8080", support).
            authorizationStrategy(formAuth).
            cookieBasedAuthentication().
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
        thrown(RestlerException)
    }

    def "test exception CookieAuthenticationRequestExecutor when cookie name is empty"() {
        when:
        new CookieAuthenticationStrategy("");
        then:
        thrown(IllegalArgumentException)
    }

}
