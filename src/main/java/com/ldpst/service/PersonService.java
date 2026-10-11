package com.ldpst.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import com.ldpst.controller.websocket.DataChangedEvent;
import com.ldpst.model.domain.Location;
import com.ldpst.model.domain.Person;
import com.ldpst.model.dto.Command.PersonCommand;
import com.ldpst.repository.PersonRepository;
import com.ldpst.service.util.NotFoundException;
import com.ldpst.service.util.BusinessRuleException;


@Service
public class PersonService {
    private final PersonRepository personRepository;
    private final ApplicationEventPublisher publisher;

    public PersonService(PersonRepository personRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.personRepository = personRepository;
        this.publisher = applicationEventPublisher;
    }

    @Transactional 
    public List<Person> people() {
        return personRepository.findAll();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Person save(Long id, PersonCommand c) {
        Person p = id == null ? new Person() : personRepository.findById(id).orElseThrow(() -> new NotFoundException("Человек не найден"));

        String passportID = normalizePassport(c.passportID());
        checkUniquePassport(id, passportID);
        apply(p, c);
        Person managed = personRepository.save(p);
        registerChange("references-changed");
        return managed;
    }

    @Transactional 
    public void delete(Long id) {
        Person e = personRepository.findById(id).orElseThrow(() -> new NotFoundException("Человек не найден"));
        personRepository.delete(e);
        registerChange("references-changed");
    }

    private void apply(Person v, PersonCommand c) {
        Location l = new Location();
        l.setX(c.location().x());
        l.setY(c.location().y());
        l.setName(c.location().name().trim());
        v.setEyeColor(c.eyeColor());
        v.setHairColor(c.hairColor());
        v.setLocation(l);
        v.setWeight(c.weight());
        v.setPassportID(normalizePassport(c.passportID()));
        v.setNationality(c.nationality());
    }

    private String normalizePassport(String passportID) {
        return passportID == null || passportID.isBlank() ? null : passportID.trim();
    }

    private void checkUniquePassport(Long personId, String passportID) {
        if (passportID == null) {
            return;
        }

        boolean exists = personId == null
                ? personRepository.existsByPassportIDIgnoreCase(passportID)
                : personRepository.existsByPassportIDIgnoreCaseAndIdNot(passportID, personId);

        if (exists) {
            throw new BusinessRuleException("Человек с Passport ID «" + passportID + "» уже существует");
        }
    }

    private void registerChange(String msg) {
        publisher.publishEvent(new DataChangedEvent(msg));
    }
}
