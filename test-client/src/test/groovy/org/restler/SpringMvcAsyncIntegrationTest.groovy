package org.restler

import com.fasterxml.jackson.databind.Module
import org.restler.async.AsyncSupport
import org.restler.http.OkHttpRequestExecutor
import org.restler.integration.ControllerApi
import org.restler.spring.mvc.SpringMvcSupport
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

import static Tests.login
import static Tests.password

class SpringMvcAsyncIntegrationTest extends Specification /* implements IntegrationSpec */ {

    def spySimpleHttpRequestExecutor = Spy(OkHttpRequestExecutor, constructorArgs: [new ArrayList<Module>()])

    SpringMvcSupport springMvcSupport = new SpringMvcSupport().
            requestExecutor(spySimpleHttpRequestExecutor)

    Service service = new Restler("http://localhost:8080", springMvcSupport).
            add(new AsyncSupport()).
            httpBasicAuthentication(login, password).
            build();

    def controller = service.produceClient(ControllerApi.class);

    @Ignore
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
