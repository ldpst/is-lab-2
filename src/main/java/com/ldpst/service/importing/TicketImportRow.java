package com.ldpst.service.importing;

import com.ldpst.model.domain.TicketType;
import com.ldpst.model.dto.Command.CoordinatesCommand;
import com.ldpst.model.dto.Command.EventCommand;
import com.ldpst.model.dto.Command.PersonCommand;
import com.ldpst.model.dto.Command.VenueCommand;

public record TicketImportRow(
        int line,
        String name,
        CoordinatesCommand coordinates,
        float price,
        TicketType type,
        Float discount,
        Integer number,
        PersonCommand person,
        EventCommand event,
        VenueCommand venue) {
}
