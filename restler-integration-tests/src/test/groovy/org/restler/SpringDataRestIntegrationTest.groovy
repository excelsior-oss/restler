package org.restler

import org.restler.http.HttpExecutionException
import org.restler.integration.springdata.Address
import org.restler.integration.springdata.Person
import org.restler.integration.springdata.PersonsRepository
import org.restler.integration.springdata.Pet
import org.restler.integration.springdata.PetsRepository
import org.restler.spring.data.SpringDataSupport
import org.restler.util.IntegrationSpec
import spock.lang.Specification

class SpringDataRestIntegrationTest extends Specification implements IntegrationSpec {
    Service serviceWithBasicAuth = new Restler("http://localhost:8080",
            new SpringDataSupport([PersonsRepository.class, PetsRepository.class])).
            httpBasicAuthentication("user", "password").
            build();

    PersonsRepository personRepository = serviceWithBasicAuth.produceClient(PersonsRepository.class)
    PetsRepository petRepository = serviceWithBasicAuth.produceClient(PetsRepository.class)

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

        pets.get(0).getPerson().setName("adasdw")

        personRepository.save(person)

        def person2 = personRepository.findOne(0L);
        def newName = person2.getPets().get(0).getName()

        then:
        newName=="New value"
    }

    def "test add new pet"() {
        when:

        def pet = new Pet(10, "test pet 10", personRepository.findOne(0L))
        petRepository.save(pet);

        def pet1 = petRepository.findOne(10L);
        def person1 = pet1.getPerson();

        then:
        person1.getName() == "test name"

    }

    def "test add new person"() {
        when:
        //personRepository.delete(2L)
        def person = new Person(2, "Test person")
        def pets = person.getPets()

        pets.add(new Pet(2, "pet2", null))
        pets.add(new Pet(3, null, null))
        pets.add(new Pet(4, "pet4", null))

        personRepository.save(person)

        def person1 = personRepository.findOne(2L)
        def pets1 = person1.getPets()

        then:
        person1.getName() == "Test person"
        pets1.get(0).getName() == "pet2"
        pets1.get(1).getName() == null
        pets1.get(2).getName() == "pet4"
    }


    def "test change address at repository"() {
        when:
        def person = personRepository.findOne(0L)

        List<Address> addresses = person.getAddresses();

        addresses.get(0).setName("New value")
        personRepository.save(person)
        def person2Address = personRepository.findOne(0L).getAddresses().get(0)

        then:
        person2Address.getName() == "New value"
    }

    def "test delete person"() {
        setup:
        def person = personRepository.findOne(1L);
        personRepository.delete(person);
        when:
        personRepository.findOne(1L);
        then:
        thrown HttpExecutionException
    }

}
