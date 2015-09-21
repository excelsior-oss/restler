package org.restler.integration.springdata

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.lang

// java.lang.String - it is workaround for https://youtrack.jetbrains.com/issue/KT-5821

@RepositoryRestResource(exported = true, path = "persons")
interface PersonsRepository : CrudRepository<Person, lang.String> {

    fun findByName(@Param("name") name: String): List<Person>
    fun findById(@Param("id") id: lang.String): Person
}
