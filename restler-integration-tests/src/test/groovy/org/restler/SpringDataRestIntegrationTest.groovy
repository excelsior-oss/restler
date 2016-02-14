package org.restler

import org.restler.integration.springdata.Address
import org.restler.integration.springdata.Person
import org.restler.integration.springdata.PersonsRepository
import org.restler.integration.springdata.Pet
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

    def "test person getPets"() {
        when:
        def person = personRepository.findOne(0L)
        List<Pet> pets = person.getPets();

        then:
        pets.get(0).getName() == "bobik"
        pets.get(1).getName() == "sharik"
    }

    def "test pet getPerson"() {
        when:
        def person = personRepository.findOne(0L)
        List<Pet> pets = person.getPets();
        Pet pet = pets.get(0);
        then:
        pet.getPerson().getName() == "test name"
    }

    def "test person getAddresses"() {
        when:
        def person = personRepository.findOne(0L)
        List<Address> addresses = person.getAddresses();

        then:
        addresses.get(0).getName() == "Earth"
        addresses.get(1).getName() == "Mars"
    }

    def "test change pet"() {
        when:
        def person = personRepository.findOne(0L)
        def pets = person.getPets();
        pets.get(0).setName("New value")
        personRepository.save(person)
        def person2 = personRepository.findOne(0L);

        then:
        person2.getPets().get(0).getName()=="New value"
    }


    def "test change address at repository"() {
        when:
        def person = personRepository.findOne(0L)

        List<Address> addresses = person.getAddresses();

        addresses.get(0).setName("New value")
        Person ppp  = personRepository.save(person)
        def person2Address = personRepository.findOne(0L).getAddresses().get(0)

        then:
        person2Address.getName() == "New value"
    }

}
