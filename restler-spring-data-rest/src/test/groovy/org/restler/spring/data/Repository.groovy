package org.restler.spring.data

import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = true, path = "testRepo")
interface Repository extends CrudRepository<Object, Long> {
}
