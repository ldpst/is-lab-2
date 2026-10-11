package com.ldpst.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ldpst.model.domain.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
    public Optional<Event> findById(Long id);

    List<Event> findAllByNameIgnoreCase(String name);
}
