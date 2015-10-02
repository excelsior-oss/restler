package org.restler

import org.restler.integration.springdata.Person
import org.restler.integration.springdata.PersonsRepository
import org.restler.spring.data.SpringDataSupport
import org.restler.util.IntegrationSpec
import spock.lang.Specification

class SpringDataRestIntegrationTest extends Specification implements IntegrationSpec {

    Service serviceWithBasicAuth = new Restler("http://localhost:8080", new SpringDataSupport()).
            httpBasicAuthentication("user", "password").
            build();

    PersonsRepository personRepository = serviceWithBasicAuth.produceClient(PersonsRepository.class)

    def "test PersonRepository findOne"() {
        expect:
        Person person = personRepository.findOne(0L)
        person.getId() == 0L
        person.getName() == "test name"
    }

    def "test query method PersonRepository findById"() {
        expect:
        Person person = personRepository.findById(0L)
        person.getId() == 0L
        person.getName() == "test name"
    }

    def "test query method PersonRepository findByName"() {
        expect:
        List<Person> persons = personRepository.findByName("test name")
        persons[0].getId() == 0L
        persons[0].getName() == "test name"
    }

}
