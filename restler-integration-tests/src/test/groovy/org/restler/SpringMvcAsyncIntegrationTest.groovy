package org.restler

import org.restler.async.AsyncSupport
import org.restler.integration.Controller
import org.restler.spring.data.SpringDataRequestExecutor
import org.restler.spring.mvc.SpringMvcSupport
import org.restler.util.IntegrationSpec
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

import static org.restler.Tests.login
import static org.restler.Tests.password

class SpringMvcAsyncIntegrationTest extends Specification implements IntegrationSpec {

    def spySimpleHttpRequestExecutor = Spy(SpringDataRequestExecutor, constructorArgs: [new RestTemplate()])

    SpringMvcSupport springMvcSupport = new SpringMvcSupport().
            requestExecutor(spySimpleHttpRequestExecutor)

    Service service = new Restler("http://localhost:8080", springMvcSupport).
            add(new AsyncSupport()).
            httpBasicAuthentication(login, password).
            build();

    def controller = service.produceClient(Controller.class);

    def "test deferred get"() {
        def c = Class.forName("org.springframework.web.context.request.async.DeferredResult")
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

    def "test future get"() {
        def future = controller.futureGet()
        def asyncCondition = new AsyncConditions();

        Thread.start {
            while (!future.done);
            asyncCondition.evaluate {
                assert future.get() == "Future OK"
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
}
