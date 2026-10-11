package com.ldpst.service.importing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.ldpst.model.domain.Event;
import com.ldpst.model.domain.Person;
import com.ldpst.model.domain.Venue;
import com.ldpst.model.dto.Command.TicketCommand;
import com.ldpst.service.TicketService;
import com.ldpst.service.util.BusinessRuleException;

@Service
public class TicketImportTransactionService {
    private final TicketCsvParser parser;
    private final ImportedReferenceService referenceService;
    private final TicketService ticketService;

    public TicketImportTransactionService(TicketCsvParser parser, ImportedReferenceService referenceService,
            TicketService ticketService) {
        this.parser = parser;
        this.referenceService = referenceService;
        this.ticketService = ticketService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public int importTickets(byte[] content) {
        List<TicketImportRow> rows = parser.parse(content);
        Set<String> ticketNumbers = new HashSet<>();

        for (TicketImportRow row : rows) {
            try {
                Person person = referenceService.resolvePerson(row.person());
                Event event = referenceService.resolveEvent(row.event());
                Venue venue = referenceService.resolveVenue(row.venue());
                checkDuplicateInFile(ticketNumbers, event.getId(), row.number());

                TicketCommand ticket = new TicketCommand(row.name(), row.coordinates(), person.getId(),
                        event.getId(), row.price(), row.type(), row.discount(), row.number(), venue.getId());
                ticketService.create(ticket);
            } catch (BusinessRuleException exception) {
                throw new BusinessRuleException("Ошибка в строке " + row.line() + ": " + exception.getMessage());
            }
        }
        return rows.size();
    }

    private void checkDuplicateInFile(Set<String> ticketNumbers, Long eventId, Integer number) {
        if (number == null) {
            return;
        }
        String key = eventId + ":" + number;
        if (!ticketNumbers.add(key)) {
            throw new BusinessRuleException(
                    "CSV содержит повтор номера " + number + " для одного события");
        }
    }
}
