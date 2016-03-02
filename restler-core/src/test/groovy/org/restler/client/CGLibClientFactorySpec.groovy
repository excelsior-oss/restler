package org.restler.client

import net.sf.cglib.proxy.InvocationHandler
import spock.lang.Specification

import java.lang.reflect.Method

class CGLibClientFactorySpec extends Specification {

    def "CGLibClientFactory should be able to create proxies of classes without default constructors"() {
        given:
        def factory = new CGLibClientFactory()

        when:
        def proxy = factory.produceClient(ControllerWithoutDefaultConstructor.class, new InvocationHandlerStub())

        then:
        proxy != null
    }

    def "CGLibClientFactory should create independent proxies of same class"() {
        given:
        def invocationHandler1 = new InvocationHandlerStub()
        def invocationHandler2 = new InvocationHandlerStub()

        def factory = new CGLibClientFactory()

        when:
        def proxy1 = factory.produceClient(ControllerWithoutDefaultConstructor.class, invocationHandler1)
        def proxy2 = factory.produceClient(ControllerWithoutDefaultConstructor.class, invocationHandler2)

        proxy1.someMethod("any")
        proxy2.someMethod("any")

        then:
        invocationHandler1.callsCount == 1
        invocationHandler2.callsCount == 1
    }

}
