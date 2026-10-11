package com.ldpst.service.importing;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.ldpst.model.domain.Event;
import com.ldpst.model.domain.Person;
import com.ldpst.model.domain.Venue;
import com.ldpst.model.dto.Command.EventCommand;
import com.ldpst.model.dto.Command.PersonCommand;
import com.ldpst.model.dto.Command.VenueCommand;
import com.ldpst.repository.EventRepository;
import com.ldpst.repository.PersonRepository;
import com.ldpst.repository.VenuesRepository;
import com.ldpst.service.EventService;
import com.ldpst.service.PersonService;
import com.ldpst.service.VenueService;
import com.ldpst.service.util.BusinessRuleException;

@Service
public class ImportedReferenceService {
    private final PersonRepository personRepository;
    private final EventRepository eventRepository;
    private final VenuesRepository venueRepository;
    private final PersonService personService;
    private final EventService eventService;
    private final VenueService venueService;

    public ImportedReferenceService(PersonRepository personRepository, EventRepository eventRepository,
            VenuesRepository venueRepository, PersonService personService, EventService eventService,
            VenueService venueService) {
        this.personRepository = personRepository;
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.personService = personService;
        this.eventService = eventService;
        this.venueService = venueService;
    }

    public Person resolvePerson(PersonCommand command) {
        String passportId = normalized(command.passportID());
        if (passportId == null) {
            List<Person> exactMatches = personRepository.findAll().stream()
                    .filter(person -> samePerson(person, command))
                    .toList();
            if (exactMatches.size() > 1) {
                throw new BusinessRuleException("Найдено несколько полностью совпадающих людей без Passport ID");
            }
            return exactMatches.isEmpty() ? personService.save(null, command) : exactMatches.get(0);
        }

        List<Person> matches = personRepository.findAllByPassportIDIgnoreCase(passportId);
        if (matches.isEmpty()) {
            return personService.save(null, command);
        }
        if (matches.size() > 1) {
            throw new BusinessRuleException("Passport ID «" + passportId + "» соответствует нескольким людям");
        }

        Person person = matches.get(0);
        if (!samePerson(person, command)) {
            throw new BusinessRuleException(
                    "Человек с Passport ID «" + passportId + "» найден, но остальные поля отличаются");
        }
        return person;
    }

    public Event resolveEvent(EventCommand command) {
        List<Event> matches = eventRepository.findAllByNameIgnoreCase(command.name().trim());
        if (matches.isEmpty()) {
            return eventService.save(null, command);
        }
        if (matches.size() > 1) {
            throw new BusinessRuleException(
                    "Название события «" + command.name().trim() + "» соответствует нескольким записям");
        }

        Event event = matches.get(0);
        if (!sameEvent(event, command)) {
            throw new BusinessRuleException(
                    "Событие «" + command.name().trim() + "» найдено, но остальные поля отличаются");
        }
        return event;
    }

    public Venue resolveVenue(VenueCommand command) {
        List<Venue> matches = venueRepository.findAllByNameIgnoreCase(command.name().trim());
        if (matches.isEmpty()) {
            return venueService.save(null, command);
        }
        if (matches.size() > 1) {
            throw new BusinessRuleException(
                    "Название площадки «" + command.name().trim() + "» соответствует нескольким записям");
        }

        Venue venue = matches.get(0);
        if (!sameVenue(venue, command)) {
            throw new BusinessRuleException(
                    "Площадка «" + command.name().trim() + "» найдена, но остальные поля отличаются");
        }
        return venue;
    }

    private boolean samePerson(Person person, PersonCommand command) {
        return person.getEyeColor() == command.eyeColor()
                && person.getHairColor() == command.hairColor()
                && Objects.equals(person.getLocation().getX(), command.location().x())
                && Objects.equals(person.getLocation().getY(), command.location().y())
                && sameText(person.getLocation().getName(), command.location().name())
                && Objects.equals(person.getWeight(), command.weight())
                && sameOptionalIgnoreCase(person.getPassportID(), command.passportID())
                && person.getNationality() == command.nationality();
    }

    private boolean sameEvent(Event event, EventCommand command) {
        return event.getName().trim().equalsIgnoreCase(command.name().trim())
                && event.getMinAge() == command.minAge()
                && sameText(event.getDescription(), command.description());
    }

    private boolean sameVenue(Venue venue, VenueCommand command) {
        return venue.getName().trim().equalsIgnoreCase(command.name().trim())
                && venue.getCapacity() == command.capacity()
                && sameOptionalText(venue.getAddress().getStreet(), command.address().street())
                && sameText(venue.getAddress().getZipCode(), command.address().zipCode());
    }

    private boolean sameText(String first, String second) {
        return Objects.equals(first.trim(), second.trim());
    }

    private boolean sameOptionalText(String first, String second) {
        return Objects.equals(normalized(first), normalized(second));
    }

    private boolean sameOptionalIgnoreCase(String first, String second) {
        String normalizedFirst = normalized(first);
        String normalizedSecond = normalized(second);
        if (normalizedFirst == null || normalizedSecond == null) {
            return normalizedFirst == null && normalizedSecond == null;
        }
        return normalizedFirst.equalsIgnoreCase(normalizedSecond);
    }

    private String normalized(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
