package org.restler

import org.restler.integration.springdata.*
import org.restler.spring.data.SpringDataSupport
import org.restler.util.IntegrationSpec
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import spock.lang.Ignore
import spock.lang.Specification

import java.util.stream.StreamSupport

class SpringDataRestIntegrationTest extends Specification implements IntegrationSpec {
    Service serviceWithBasicAuth = new Restler("http://localhost:8080",
            new SpringDataSupport([PersonsRepository.class, PetsRepository.class, PostsRepository.class], 1000)).
            httpBasicAuthentication("user", "password").
            build();

    PersonsRepository personRepository = serviceWithBasicAuth.produceClient(PersonsRepository.class)
    PetsRepository petRepository = serviceWithBasicAuth.produceClient(PetsRepository.class)
    PostsRepository postRepository = serviceWithBasicAuth.produceClient(PostsRepository.class)

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

    def "test many to many save change"() {
        given:
        def person = personRepository.findOne(1L)
        def posts = person.getPosts()

        when:
        posts.add(new Post(4L, "test", null))
        personRepository.save(person)

        posts = person.getPosts()
        then:
        posts[0].getId() == 4L
        posts[0].getMessage() == "test"
        posts[0].getAuthors().equals([person])

        posts[1].getId() == 2L
        posts[2].getId() == 0L
        cleanup:
        def postForDelete = posts[0]
        posts.remove(0)
        personRepository.save(person)
        postRepository.delete(postForDelete)
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
        def person = personRepository.findOne(35L)

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

        cleanup:
        pets[0].setName("pet2")
        petRepository.save(pets[0])
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

        cleanup:
        petRepository.delete(10L);
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
        def person = new Person(3L, "person3", null)
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
        personRepository.save(new Person(5L, "temp", null))
        def person = personRepository.findOne(5L)
        when: "Delete person from repository"
        personRepository.delete(person)
        def person1 = personRepository.findOne(5L)
        then: "Person was deleted successfully"
        person1 == null
    }

    def "test delete resource from repository by id"() {
        given: "Person that exist already"
        personRepository.save(new Person(5L, "temp", null))
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
        persons.size() == 4

        persons[0].getId() == 0L
        persons[1].getId() == 1L
        persons[2].getId() == 2L
        persons[3].getId() == 35L
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

    def "test delete all resources from repository"() {
        given:
        def oldPets = petRepository.findAll()
        //for saving associations to person
        for(Pet pet : oldPets) {
            pet.getPerson()
        }

        when:
        petRepository.deleteAll()
        then:
        def pets = petRepository.findAll()
        pets.size() == 0
        cleanup:
        petRepository.save(oldPets)
    }

    def "test delete several resources from repository"() {
        given:
        def ids = new ArrayList<Long>()

        ids.add(0L)
        ids.add(2L)

        def petsForDelete = petRepository.findAll(ids)
        //for saving associations to person
        for(Pet pet : petsForDelete) {
            pet.getPerson()
        }

        when:
        petRepository.delete(petsForDelete)
        then:
        def pets = petRepository.findAll(ids)
        StreamSupport.stream(pets.spliterator(), false).allMatch({it == null})

        cleanup:
        petRepository.save(petsForDelete)
    }

    def "test save several elements to repository"() {
        given:
        def elements = new ArrayList()
        elements.add(new Pet(100L, "test", null))
        elements.add(new Pet(101L, "test", null))
        elements.add(new Pet(102L, "test", null))
        elements.add(new Pet(103L, "test", null))

        when:
        elements = petRepository.save(elements)

        then:
        petRepository.findOne(100L) != null
        petRepository.findOne(101L) != null
        petRepository.findOne(102L) != null
        petRepository.findOne(103L) != null

        cleanup:
        petRepository.delete(elements)
    }

    def "test save changes several elements to repository"() {
        given:
        def oldPets = petRepository.findAll();
        def newPets = petRepository.findAll();

        int i = 0
        for(Pet pet : newPets) {
            pet.name = (i++).toString()
        }

        when:
        petRepository.save(newPets)
        newPets = petRepository.findAll()

        then:
        int j = 0
        for(Pet pet : newPets) {
            pet.name == (j++).toString()
        }

        cleanup:
        petRepository.save(oldPets)
    }

    def "test get desc order sorted page"() {
        given:
        def sort = new Sort(Sort.Direction.DESC, "id")
        when:
        def posts = postRepository.findAll(sort)
        then:
        posts.size() == 3
        posts[0].getId() == 2L
        posts[1].getId() == 1L
        posts[2].getId() == 0L
    }

    def "test get asc order sorted page"() {
        given:
        def sort = new Sort(Sort.Direction.ASC, "id")
        when:
        def posts = postRepository.findAll(sort)
        then:
        posts.size() == 3
        posts[0].getId() == 0L
        posts[1].getId() == 1L
        posts[2].getId() == 2L
    }

    def "test paging"() {
        given:
        def posts = new ArrayList<Post>()
        Iterable<Person> persons = personRepository.findAll([0L, 1L, 2L])
        for(Long id = 3; id < 20; ++id) {
            posts.add(new Post(id, "Hello!", [persons[(int)id%3]]))
        }
        posts = postRepository.save(posts)

        when:
        def resultPage = postRepository.findAll(new PageRequest(3, 5, Sort.Direction.ASC, "id"))
        def resultPosts = resultPage.content
        then:
        resultPosts.size() == 5
        resultPosts[0].getId() == 15L
        resultPosts[1].getId() == 16L
        resultPosts[2].getId() == 17L
        resultPosts[3].getId() == 18L
        resultPosts[4].getId() == 19L

        cleanup:
        postRepository.delete(posts)
    }

    def "test saving resource without information about association"() {
        expect:
        def pets = petRepository.findAll() //association to persons is null (but association on server is not null)
        petRepository.save(pets) //must save changes and don't change association on server
        for(Pet pet : pets) {
            pet.getPerson() != null
        }

        def person = personRepository.findOne(0l) //association to pets is null (but association on server is not null)
        personRepository.save(person) //must save changes and don't change association on server

        def pets1 = person.getPets()
        pets1 != null
        pets1.size() == 2
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
