package org.restler.integration.springdata;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = true, path = "posts")
public interface PostsRepository extends PagingAndSortingRepository<Post, Long> {
}
