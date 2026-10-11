package com.ldpst.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ldpst.model.domain.Venue;

import jakarta.persistence.EntityManager;

@Repository
public class SpecialOperationRepository {
    private final EntityManager entityManager;

    public SpecialOperationRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public long deleteTicketsByVenue(long venueId) {
        return scalar("select \"is-lab-2\".delete_tickets_by_venue(:venueId)", "venueId", venueId);
    }

    public long countTicketsByVenueCapacityGreater(long capacity) {
        return scalar(
                "select \"is-lab-2\".count_tickets_by_venue_capacity_greater(:capacity)",
                "capacity",
                capacity);
    }

    @SuppressWarnings("unchecked")
    public List<Venue> findUniqueTicketVenues() {
        return entityManager
                .createNativeQuery("select * from \"is-lab-2\".unique_ticket_venues()", Venue.class)
                .getResultList();
    }

    public long cancelEvent(long eventId) {
        return scalar("select \"is-lab-2\".cancel_event(:eventId)", "eventId", eventId);
    }

    public long deleteTicketsByPerson(long personId) {
        return scalar("select \"is-lab-2\".delete_tickets_by_person(:personId)", "personId", personId);
    }

    private long scalar(String sql, String parameterName, long parameterValue) {
        Number result = (Number) entityManager
                .createNativeQuery(sql)
                .setParameter(parameterName, parameterValue)
                .getSingleResult();
        return result.longValue();
    }
}
