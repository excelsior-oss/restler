package org.restler

import org.restler.integration.springdata.*
import org.restler.spring.data.SpringDataSupport
import org.restler.util.IntegrationSpec
import spock.lang.Ignore
import spock.lang.Specification

class SpringDataRestIntegrationTest extends Specification implements IntegrationSpec {
    Service serviceWithBasicAuth = new Restler("http://localhost:8080",
            new SpringDataSupport([PersonsRepository.class, PetsRepository.class], 1000)).
            httpBasicAuthentication("user", "password").
            build();

    PersonsRepository personRepository = serviceWithBasicAuth.produceClient(PersonsRepository.class)
    PetsRepository petRepository = serviceWithBasicAuth.produceClient(PetsRepository.class)

    def "test PersonRepository findOne"() {
        expect:
        Person person = personRepository.findOne(1L)
        person.getId() == 1L
        person.getName() == "person1"
    }

    def "test query method PersonRepository findById"() {
        expect:
        Person person = personRepository.findById(1L)
        person.getId() == 1L
        person.getName() == "person1"
    }

    def "test query method PersonRepository findByName"() {
        expect:
        List<Person> persons = personRepository.findByName("person1")
        persons[0].getId() == 1L
        persons[0].getName() == "person1"
    }

    def "test composite objects retrieving that contain resource with repository"() {
        given: "Person that have associated list of pets that have repository"
        def person = personRepository.findOne(0L)

        when: "List of associated pets are accessed"
        def pets = person.getPets();

        then: "It elements are correctly retrieved from remote server"
        pets[0].getName() == "bobik"
        pets[1].getName() == "sharik"
    }

    def "test get objects with cycle references correctly"() {
        given: "Person and pet that retrieved from remote server and have references to each other"
        def person = personRepository.findOne(1L)
        def pet = person.getPets()[0]
        when: "Person is got from pet"
        def person1 = pet.getPerson()
        then: "Person is correctly got from pet that got from this person"
        person1.getId() == 1L
        person1.getName() == "person1"
    }

    def "test composite objects retrieving that contain resource without repository"() {
        given: "Person that associated list of addresses without repository"
        def person = personRepository.findOne(0L)

        when: "List of associated addresses are accessed"
        def addresses = person.getAddresses()

        then: "It elements are correctly retrieved from remote server"
        addresses[0].getName() == "Earth"
        addresses[1].getName() == "Mars"
    }

    def "test change part of composite object"() {
        given: "Person and pets where person is composite object and pets is part of person"
        def person = personRepository.findOne(1L)
        def pets = person.getPets();
        when: "Change pet's name and get new value from server"
        pets.get(0).setName("New value")
        personRepository.save(person)

        def person2 = personRepository.findOne(1L);
        def newName = person2.getPets().get(0).getName()
        then: "Pet name was changed and saved on remote server"
        newName=="New value"
    }

    def "test add new resource that part of other resource"() {
        when: "New pet was added to person that exist already"

        def pet = new Pet(10, "test pet 10", personRepository.findOne(2L))
        petRepository.save(pet);

        def pet1 = petRepository.findOne(10L);
        def person1 = pet1.getPerson();

        then: "Pet was added to person"
        pet1.getId() == 10L
        pet1.getName() == "test pet 10"
        person1.getId() == 2L
        person1.getName() == "person2"

    }

    def "test change resource from resource that have reference to it"() {
        given: "Person and pet that retrieved from remote server and have references to each other"
        def person = personRepository.findOne(1L)
        def pets = person.getPets()
        when: "Change and save person using reference from pet"
        pets[0].getPerson().setName("New person name")
        personRepository.save(person)

        def person1 = personRepository.findOne(1L)
        then: "Person was changed successfully"
        person1.getName() == "New person name"

        cleanup:
        person1.setName("person1")
        personRepository.save(person1)
    }

    def "test add new resources to list from composite resource"() {
        given: "Person and list of associated pets"
        def person = new Person(3L, "person3")
        def pets = person.getPets()

        when: "Add new pets and associate to person"
        pets.add(new Pet(4, "pet4", null))
        pets.add(new Pet(5, "pet5", null))
        pets.add(new Pet(6, "pet6", null))

        personRepository.save(person)

        def person1 = personRepository.findOne(3L)
        def pets1 = person1.getPets()

        then: "Pets was added to person successfully"
        person1.getId() == 3L
        person1.getName() == "person3"
        pets1[0].getName() == "pet4"
        pets1[1].getName() == "pet5"
        pets1[2].getName() == "pet6"

        cleanup:
        petRepository.delete(pets1[0])
        petRepository.delete(pets1[1])
        petRepository.delete(pets1[2])

        personRepository.delete(person1)

    }


    def "test delete resource from repository"() {
        given: "Person that exist already"
        personRepository.save(new Person(5L, "temp"))
        def person = personRepository.findOne(5L)
        when: "Delete person from repository"
        personRepository.delete(person)
        def person1 = personRepository.findOne(5L)
        then: "Person was deleted successfully"
        person1 == null
    }

    def "test delete resource from repository by id"() {
        given: "Person that exist already"
        personRepository.save(new Person(5L, "temp"))
        when: "Delete person from repository"
        personRepository.delete(5L)
        def person1 = personRepository.findOne(5L)
        then: "Person was deleted successfully"
        person1 == null
    }

    def "test findAll"() {
        when:
        def persons = personRepository.findAll()

        then:
        persons.size() == 3
    }

    def "test findAll by ids"() {
        given:
        def ids = new ArrayList();
        ids.add(0)
        ids.add(2)
        when:
        def persons = personRepository.findAll(ids)
        then:
        persons.size() == 2
        persons[0].getId() == 0L
        persons[1].getId() == 2L
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
