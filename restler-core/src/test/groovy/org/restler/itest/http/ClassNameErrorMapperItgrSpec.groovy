package org.restler.itest.http

import org.restler.Service
import org.restler.ServiceBuilder
import org.restler.testserver.Controller
import spock.lang.Specification

class ClassNameErrorMapperItgrSpec extends Specification {

    Service service = new ServiceBuilder("http://localhost:8080").
            useClassNameExceptionMapper().
            build();

    def "RuntimeException"() {
        given:
        def ctrl = service.produceClient(Controller.class)
        when:
        ctrl.throwException(RuntimeException.class.getCanonicalName())
        then:
        thrown(RuntimeException)
    }

    def "IOException"() {
        given:
        def ctrl = service.produceClient(Controller.class)
        when:
        ctrl.throwException(IOException.class.getCanonicalName())
        then:
        thrown(IOException)
    }

}
