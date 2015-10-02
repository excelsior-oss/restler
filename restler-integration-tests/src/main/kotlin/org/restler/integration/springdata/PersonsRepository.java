package org.restler.integration.springdata;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(exported = true, path = "persons")
public interface PersonsRepository extends CrudRepository<Person, Long> {
    Person findById(@Param("id") Long id);

    List<Person> findByName(@Param("name") String name);

}
