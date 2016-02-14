package org.restler.integration.springdata

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

/**
 * Created by rudenko on 09.02.2016.
 */

@RepositoryRestResource(exported = true, path = "pets")
interface PetsRepository : CrudRepository<Pet, Long> {
    fun findById(@Param("id") id: Long?): Pet

    fun findByName(@Param("name") name: String): List<Pet>
}