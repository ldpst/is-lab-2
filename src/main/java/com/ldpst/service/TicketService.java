package com.ldpst.service;

import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import com.ldpst.controller.websocket.DataChangedEvent;
import com.ldpst.model.domain.*;
import com.ldpst.model.dto.Command.TicketSearch;
import com.ldpst.repository.EventRepository;
import com.ldpst.repository.PersonRepository;
import com.ldpst.repository.TicketRepository;
import com.ldpst.repository.VenuesRepository;
import com.ldpst.service.specification.TicketSpecification;
import com.ldpst.service.util.NotFoundException;
import com.ldpst.service.util.BusinessRuleException;


import com.ldpst.model.dto.Command.PageResult;
import com.ldpst.model.dto.Command.TicketCommand;

@Service
public class TicketService {
    private static final Map<String, String> SORT_PROPERTIES = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("name", "name"),
            Map.entry("creationDate", "creationDate"),
            Map.entry("x", "coordinates.x"),
            Map.entry("y", "coordinates.y"),
            Map.entry("personPassportID", "person.passportID"),
            Map.entry("personPassportId", "person.passportID"),
            Map.entry("eventName", "event.name"),
            Map.entry("price", "price"),
            Map.entry("type", "type"),
            Map.entry("discount", "discount"),
            Map.entry("number", "number"),
            Map.entry("venueName", "venue.name"));

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final VenuesRepository venuesRepository;
    private final PersonRepository personRepository;
    private final ApplicationEventPublisher publisher;

    public TicketService(TicketRepository ticketRepository, EventRepository eventRepository,
            VenuesRepository venuesRepository, PersonRepository personRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.personRepository = personRepository;
        this.venuesRepository = venuesRepository;
        this.publisher = applicationEventPublisher;
    }

    @Transactional(readOnly = true)
    public PageResult<Ticket> findAll(TicketSearch ts) {
        String sortProperty = SORT_PROPERTIES.getOrDefault(ts.sort(), "id");
        Sort sort = Sort.by(ts.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC, sortProperty);

        Pageable pageable = PageRequest.of(ts.page(), ts.size(), sort);

        Specification<Ticket> spec = TicketSpecification.filter(ts.filterField(), ts.filter());

        Page<Ticket> page = ticketRepository.findAll(spec, pageable);

        return new PageResult<Ticket>(page.getContent(), page.getTotalElements(), page.getTotalPages(), ts.page(),
                ts.size());
    }

    @Transactional(readOnly = true)
    public Ticket get(int id) {
        return ticketRepository.findById(id).orElseThrow(() -> new NotFoundException("Билет не найден"));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Ticket create(TicketCommand c) {
        checkUniqueNumber(null, c.eventId(), c.number());
        Ticket t = new Ticket();
        apply(t, c);

        Ticket managed = ticketRepository.save(t);
        registerChange("ticket-updated");
        return managed;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Ticket update(int id, TicketCommand c, Long version) {
        Ticket ticket = get(id);
        checkVersion(ticket, version);
        checkUniqueNumber(id, c.eventId(), c.number());
        apply(ticket, c);
        Ticket managed = ticketRepository.save(ticket);
        registerChange("ticket-updated");
        return managed;
    }

    @Transactional 
    public void delete(int id, long version) {
        Ticket ticket = get(id);
        checkVersion(ticket, version);
        ticketRepository.delete(ticket);
        registerChange("ticket-deleted");
    }

    private void checkVersion(Ticket ticket, Long version) {
        if (version == null || ticket.getVersion() != version) {
            throw new BusinessRuleException(
                    "Билет уже изменён другим пользователем. Обновите данные и повторите операцию.");
        }
    }

    private void apply(Ticket t, TicketCommand c) {
        Coordinates p = new Coordinates();
        p.setX(c.coordinates().x());
        p.setY(c.coordinates().y());

        t.setName(c.name().trim());
        t.setCoordinates(p);
        t.setPrice(c.price());
        t.setType(c.type());
        t.setDiscount(c.discount());
        t.setNumber(c.number());

        Person person = personRepository.findById(c.personId())
                .orElseThrow(() -> new NotFoundException("Человек не найден"));
        Event event = eventRepository.findById(c.eventId())
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        Venue venue = venuesRepository.findById(c.venueId())
                .orElseThrow(() -> new NotFoundException("Площадка не найдена"));

        t.setPerson(person);
        t.setEvent(event);
        t.setVenue(venue);
    }

    private void checkUniqueNumber(Integer ticketId, Long eventId, Integer number) {
        if (number == null) {
            return;
        }

        boolean exists = ticketId == null
                ? ticketRepository.existsByEventIdAndNumber(eventId, number)
                : ticketRepository.existsByEventIdAndNumberAndIdNot(eventId, number, ticketId);

        if (exists) {
            throw new BusinessRuleException(
                    "Билет с номером " + number + " уже существует для выбранного события");
        }
    }

    public void registerChange(String msg) {
        publisher.publishEvent(new DataChangedEvent(msg));
    }
}
