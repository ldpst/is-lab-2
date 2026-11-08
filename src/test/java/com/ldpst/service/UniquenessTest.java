package com.ldpst.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import com.ldpst.model.domain.Address;
import com.ldpst.model.domain.Ticket;
import com.ldpst.model.domain.TicketType;
import com.ldpst.model.domain.Venue;
import com.ldpst.model.dto.Command.AddressCommand;
import com.ldpst.model.dto.Command.CoordinatesCommand;
import com.ldpst.model.dto.Command.TicketCommand;
import com.ldpst.model.dto.Command.VenueCommand;
import com.ldpst.repository.EventRepository;
import com.ldpst.repository.PersonRepository;
import com.ldpst.repository.TicketRepository;
import com.ldpst.repository.VenuesRepository;
import com.ldpst.service.util.BusinessRuleException;

class UniquenessTest {
    private final TicketRepository tickets = mock(TicketRepository.class);
    private final VenuesRepository venues = mock(VenuesRepository.class);
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final TicketService ticketService = new TicketService(tickets,
            mock(EventRepository.class), venues, mock(PersonRepository.class), publisher);
    private final VenueService venueService = new VenueService(venues, publisher);

    @Test
    void vipCannotBeCreatedWithAnotherNumberForSamePersonAndEvent() {
        when(tickets.existsByPersonIdAndEventIdAndType(1L, 2L, TicketType.VIP)).thenReturn(true);

        assertThatThrownBy(() -> ticketService.create(vipTicket()))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("VIP-билет");
    }

    @Test
    void ticketCannotBecomeASecondVipOnUpdate() {
        Ticket ticket = new Ticket();
        ticket.setType(TicketType.USUAL);
        when(tickets.findById(9)).thenReturn(Optional.of(ticket));
        when(tickets.existsByPersonIdAndEventIdAndTypeAndIdNot(1L, 2L, TicketType.VIP, 9)).thenReturn(true);

        assertThatThrownBy(() -> ticketService.update(9, vipTicket(), 0L))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("VIP-билет");
    }

    @Test
    void anotherVenueCannotUseTheSameNormalizedAddressOnUpdate() {
        when(venues.findById(2L)).thenReturn(Optional.of(venue(2L, "Другая улица", "AB12345")));
        when(venues.findAll()).thenReturn(List.of(venue(1L, "  Улица   Ленина 10 ", "AB12345")));

        assertThatThrownBy(() -> venueService.save(2L, venueCommand("улица ЛЕНИНА 10")))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("с таким адресом");
    }

    @Test
    void venueCanKeepItsOwnAddress() {
        Venue venue = venue(1L, "Улица Ленина 10", "AB12345");
        when(venues.findById(1L)).thenReturn(Optional.of(venue));
        when(venues.findAll()).thenReturn(List.of(venue));

        venueService.save(1L, venueCommand("улица ЛЕНИНА 10"));

        verify(venues).save(venue);
    }

    private TicketCommand vipTicket() {
        return new TicketCommand("VIP", new CoordinatesCommand(1, 2L), 1L, 2L,
                100, TicketType.VIP, null, 42, 3L);
    }

    private VenueCommand venueCommand(String street) {
        return new VenueCommand("Арена", 100, new AddressCommand(street, "ab12345"));
    }

    private Venue venue(Long id, String street, String zipCode) {
        Address address = new Address();
        address.setStreet(street);
        address.setZipCode(zipCode);
        Venue venue = new Venue();
        venue.setId(id);
        venue.setAddress(address);
        return venue;
    }
}
