package org.restler

import org.restler.http.HttpExecutionException
import org.restler.integration.springdata.*
import org.restler.spring.data.SpringDataSupport
import org.restler.util.IntegrationSpec
import spock.lang.Ignore
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
        person.getName() == "person0"
    }

    def "test query method PersonRepository findById"() {
        expect:
        Person person = personRepository.findById(0L)
        person.getId() == 0L
        person.getName() == "person0"
    }

    def "test query method PersonRepository findByName"() {
        expect:
        List<Person> persons = personRepository.findByName("person0")
        persons[0].getId() == 0L
        persons[0].getName() == "person0"
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
        pet.getPerson().getId() == 0L
        pet.getPerson().getName() == "person0"
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
        def person = personRepository.findOne(1L)
        def pets = person.getPets();
        pets.get(0).setName("New value")

        personRepository.save(person)

        def person2 = personRepository.findOne(1L);
        def newName = person2.getPets().get(0).getName()
        then:
        newName=="New value"
    }

    def "test change person from pet"() {
        when:
        def person = personRepository.findOne(1L)
        def pets = person.getPets()
        pets.get(0).getPerson().setName("New person name")
        personRepository.save(person)

        def person1 = personRepository.findOne(1L)
        then:
        person1.getName() == "New person name"

    }

    def "test add new pet"() {
        when:

        def pet = new Pet(10, "test pet 10", personRepository.findOne(0L))
        petRepository.save(pet);

        def pet1 = petRepository.findOne(10L);
        def person1 = pet1.getPerson();

        then:
        pet1.getId() == 10L
        pet1.getName() == "test pet 10"
        person1.getId() == 0L
        person1.getName() == "person0"

    }

    def "test add new person"() {
        when:
        def person = new Person(3L, "person3")
        def pets = person.getPets()

        pets.add(new Pet(2, "pet2", null))
        pets.add(new Pet(3, "pet3", null))
        pets.add(new Pet(4, "pet4", null))

        personRepository.save(person)

        def person1 = personRepository.findOne(3L)
        def pets1 = person1.getPets()

        then:
        person1.getId() == 3L
        person1.getName() == "person3"
        pets1.get(0).getName() == "pet2"
        pets1.get(1).getName() == "pet3"
        pets1.get(2).getName() == "pet4"
    }


    def "test delete person without pets"() {
        setup:
        def person = personRepository.findOne(2L);
        personRepository.delete(person);
        when:
        personRepository.findOne(2L);
        then:
        thrown HttpExecutionException
    }

    @Ignore
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

}
