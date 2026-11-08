package com.ldpst.service.importing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ldpst.model.domain.Address;
import com.ldpst.model.domain.Event;
import com.ldpst.model.domain.Venue;
import com.ldpst.model.dto.Command.AddressCommand;
import com.ldpst.model.dto.Command.EventCommand;
import com.ldpst.model.dto.Command.VenueCommand;
import com.ldpst.repository.EventRepository;
import com.ldpst.repository.PersonRepository;
import com.ldpst.repository.VenuesRepository;
import com.ldpst.service.EventService;
import com.ldpst.service.PersonService;
import com.ldpst.service.VenueService;
import com.ldpst.service.util.BusinessRuleException;

class ImportedReferenceServiceTest {
    private final PersonRepository personRepository = mock(PersonRepository.class);
    private final EventRepository eventRepository = mock(EventRepository.class);
    private final VenuesRepository venueRepository = mock(VenuesRepository.class);
    private final PersonService personService = mock(PersonService.class);
    private final EventService eventService = mock(EventService.class);
    private final VenueService venueService = mock(VenueService.class);

    private ImportedReferenceService service;

    @BeforeEach
    void setUp() {
        service = new ImportedReferenceService(personRepository, eventRepository, venueRepository,
                personService, eventService, venueService);
    }

    @Test
    void reusesEventWhenAllFieldsMatch() {
        Event existing = event("Концерт", 12, "Описание");
        EventCommand imported = new EventCommand(" концерт ", 12, "Описание");
        when(eventRepository.findAllByNameIgnoreCase("концерт")).thenReturn(List.of(existing));

        Event result = service.resolveEvent(imported);

        assertThat(result).isSameAs(existing);
        verify(eventService, never()).save(null, imported);
    }

    @Test
    void rejectsEventWhenNaturalKeyMatchesButFieldsDiffer() {
        Event existing = event("Концерт", 12, "Старое описание");
        EventCommand imported = new EventCommand("Концерт", 12, "Новое описание");
        when(eventRepository.findAllByNameIgnoreCase("Концерт")).thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.resolveEvent(imported))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("остальные поля отличаются");
    }

    @Test
    void createsVenueWhenNaturalKeyDoesNotMatch() {
        VenueCommand imported = new VenueCommand("Новая арена", 100,
                new AddressCommand(null, "1234567"));
        Venue created = new Venue();
        when(venueRepository.findAllByNameIgnoreCase("Новая арена")).thenReturn(List.of());
        when(venueService.save(null, imported)).thenReturn(created);

        assertThat(service.resolveVenue(imported)).isSameAs(created);
        verify(venueService).save(null, imported);
    }

    @Test
    void reusesVenueWhenOnlyAddressFormattingDiffers() {
        Address address = new Address();
        address.setStreet("Улица Ленина 10");
        address.setZipCode("AB12345");
        Venue venue = new Venue();
        venue.setName("Арена");
        venue.setCapacity(100);
        venue.setAddress(address);
        VenueCommand imported = new VenueCommand("Арена", 100,
                new AddressCommand("  улица   ЛЕНИНА 10 ", "ab12345"));
        when(venueRepository.findAllByNameIgnoreCase("Арена")).thenReturn(List.of(venue));

        assertThat(service.resolveVenue(imported)).isSameAs(venue);
        verify(venueService, never()).save(null, imported);
    }

    private Event event(String name, int minAge, String description) {
        Event event = new Event();
        event.setName(name);
        event.setMinAge(minAge);
        event.setDescription(description);
        return event;
    }
}
