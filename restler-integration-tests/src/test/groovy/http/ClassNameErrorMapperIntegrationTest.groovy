package org.restler.integration.http

import org.restler.Service
import org.restler.ServiceBuilder
import org.restler.integration.Controller
import spock.lang.Specification

class ClassNameErrorMapperIntegrationTest extends Specification {

    Service service = new ServiceBuilder("http://localhost:8080").
            reauthorizeRequestsOnForbidden(false).
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
