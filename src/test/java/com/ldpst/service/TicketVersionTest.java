package com.ldpst.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import com.ldpst.model.domain.Ticket;
import com.ldpst.repository.EventRepository;
import com.ldpst.repository.PersonRepository;
import com.ldpst.repository.TicketRepository;
import com.ldpst.repository.VenuesRepository;
import com.ldpst.service.util.BusinessRuleException;

class TicketVersionTest {
    private final TicketRepository tickets = mock(TicketRepository.class);
    private final TicketService service = new TicketService(tickets,
            mock(EventRepository.class), mock(VenuesRepository.class),
            mock(PersonRepository.class), mock(ApplicationEventPublisher.class));

    @Test
    void staleUpdateIsRejectedBeforeApplyingChanges() {
        when(tickets.findById(1)).thenReturn(Optional.of(new Ticket()));

        assertThatThrownBy(() -> service.update(1, null, 1L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void missingVersionCannotBypassUpdateProtection() {
        when(tickets.findById(1)).thenReturn(Optional.of(new Ticket()));

        assertThatThrownBy(() -> service.update(1, null, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void staleDeleteDoesNotRemoveTicket() {
        Ticket ticket = new Ticket();
        when(tickets.findById(1)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.delete(1, 1L))
                .isInstanceOf(BusinessRuleException.class);
        verify(tickets, never()).delete(ticket);
    }
}
