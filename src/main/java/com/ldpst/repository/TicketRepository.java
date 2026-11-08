package com.ldpst.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.ldpst.model.domain.Ticket;
import com.ldpst.model.domain.TicketType;

public interface TicketRepository extends JpaRepository<Ticket, Integer>, JpaSpecificationExecutor<Ticket> {
    boolean existsByEventIdAndNumber(Long eventId, Integer number);

    boolean existsByEventIdAndNumberAndIdNot(Long eventId, Integer number, Integer id);

    boolean existsByPersonIdAndEventIdAndType(Long personId, Long eventId, TicketType type);

    boolean existsByPersonIdAndEventIdAndTypeAndIdNot(Long personId, Long eventId, TicketType type, Integer id);
}
