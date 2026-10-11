package com.ldpst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.ldpst.model.domain.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Integer>, JpaSpecificationExecutor<Ticket> {
    boolean existsByEventIdAndNumber(Long eventId, Integer number);

    boolean existsByEventIdAndNumberAndIdNot(Long eventId, Integer number, Integer id);
}
