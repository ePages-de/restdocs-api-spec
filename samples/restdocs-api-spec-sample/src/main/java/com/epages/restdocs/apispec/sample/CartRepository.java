package com.epages.restdocs.apispec.sample;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface CartRepository extends PagingAndSortingRepository<Cart, Long> {

}
