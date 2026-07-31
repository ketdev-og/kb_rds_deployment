package com.ops.kbspring.person;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PersonRepo extends CrudRepository<Person, String> {
    List<Person> findByEmail(String email);   // works ONLY because email is @Indexed
}

