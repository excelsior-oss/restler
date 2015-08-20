package org.restler

import com.fasterxml.jackson.module.paranamer.ParanamerModule
import org.restler.integration.Controller
import org.restler.integration.SimpleDto
import spock.lang.Specification

class GenericCollectionsIntegrationTest extends Specification {

    Service serviceWithFormAuth = new ServiceBuilder("http://localhost:8080").
            classNameExceptionMapper().
            addJacksonModule(new ParanamerModule()).
            build();

    Controller controller = serviceWithFormAuth.produceClient(Controller.class)

    def "List of strings should be mapped correctly"() {
        when:
        def res = controller.listOfStrings

        then:
        res == ['item1', 'item2']
    }

    def "List of dtos should be mapped correctly"() {
        when:
        def dtos = controller.listOfDtos

        then:
        dtos == [new SimpleDto("1", "dto1"), new SimpleDto("2", "dto2")]
    }

    def "Set of dtos should be mapped correctly"() {
        when:
        def dtos = controller.setOfDtos

        then:
        dtos.contains(new SimpleDto("1", "dto1"))
        dtos.contains(new SimpleDto("2", "dto2"))
    }


    def "Map of dtos should be mapped correctly"() {
        when:
        def dtos = controller.mapOfDtos

        then:
        dtos["1"] == new SimpleDto("1", "dto1")
        dtos["2"] == new SimpleDto("2", "dto2")
    }
}
