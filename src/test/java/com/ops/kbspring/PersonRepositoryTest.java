package com.ops.kbspring;

import com.ops.kbspring.person.Person;
import com.ops.kbspring.person.PersonRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
  class PersonRepositoryTest {
  
      @Autowired
      PersonRepo repository;
  
      @BeforeEach
      void clean() {
          repository.deleteAll();   // isolate each test — start with no people
      }
  
      @Test 
      void savesAndFindsById() {
          repository.save(new Person("1", "alice@x.com", "Alice", 30));
  
          Optional<Person> found = repository.findById("1");
  
          assertThat(found).isPresent(); 
          assertThat(found.get().getName()).isEqualTo("Alice");
      }
  
      @Test 
      void findsBySecondaryIndex() {
          repository.save(new Person("1", "alice@x.com", "Alice", 30));
          repository.save(new Person("2", "bob@x.com",   "Bob",   25));
  
          List<Person> result = repository.findByEmail("alice@x.com");
  
          assertThat(result).hasSize(1); 
          assertThat(result.get(0).getId()).isEqualTo("1");
      }
  }
