package com.ldpst.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ldpst.model.domain.Venue;

public interface VenuesRepository extends JpaRepository<Venue, Long> {
    public Optional<Venue> findById(Long id);

    List<Venue> findAllByNameIgnoreCase(String name);
}
