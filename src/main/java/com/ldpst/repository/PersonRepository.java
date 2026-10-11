package com.ldpst.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ldpst.model.domain.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findById(Long id);

    boolean existsByPassportIDIgnoreCase(String passportID);

    boolean existsByPassportIDIgnoreCaseAndIdNot(String passportID, Long id);

    List<Person> findAllByPassportIDIgnoreCase(String passportID);
}
