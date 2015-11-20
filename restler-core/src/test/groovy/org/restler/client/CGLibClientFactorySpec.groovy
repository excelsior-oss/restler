package org.restler.client

import net.sf.cglib.proxy.InvocationHandler
import spock.lang.Specification

import java.lang.reflect.Method

class CGLibClientFactorySpec extends Specification {

    def "CGLibClientFactory should be able to create proxies of classes without default constructors"() {
        given:
        def module = Mock(CoreModule)
        module.canHandle(_) >> true
        module.createHandler(_) >> new InvocationHandlerStub()

        def factory = new CGLibClientFactory(module)

        when:
        def proxy = factory.produceClient(ControllerWithoutDefaultConstructor.class)

        then:
        proxy != null
    }

    class InvocationHandlerStub implements InvocationHandler {

        @Override
        Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            return null
        }

    }

    class ControllerWithoutDefaultConstructor {

        private final Object someDependency;

        public ControllerWithoutDefaultConstructor(Object someDependency) {
            this.someDependency = someDependency;
        }

    }
}

