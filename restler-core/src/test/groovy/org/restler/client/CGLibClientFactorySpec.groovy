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

    def "CGLibClientFactory should create independent proxies of same class"() {
        given:
        def module = Mock(CoreModule)
        module.canHandle(_) >> true

        def invocationHandler1 = new InvocationHandlerStub()
        def invocationHandler2 = new InvocationHandlerStub()
        module.createHandler(_) >>> [invocationHandler1, invocationHandler2]

        def factory = new CGLibClientFactory(module)

        when:
        def proxy1 = factory.produceClient(ControllerWithoutDefaultConstructor.class)
        def proxy2 = factory.produceClient(ControllerWithoutDefaultConstructor.class)

        proxy1.someMethod("any")
        proxy2.someMethod("any")

        then:
        invocationHandler1.callsCount == 1
        invocationHandler2.callsCount == 1
    }

}
