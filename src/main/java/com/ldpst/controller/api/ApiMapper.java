package com.ldpst.controller.api;

import org.springframework.stereotype.Component;
import com.ldpst.model.domain.*;
import com.ldpst.model.dto.Command.*;
import com.ldpst.controller.api.ApiModel.*;

@Component
public class ApiMapper {
    public PersonDto person(Person v) {
        return new PersonDto(v.getId(), v.getEyeColor(), v.getHairColor(), location(v.getLocation()), v.getWeight(),
                v.getPassportID(), v.getNationality());
    }

    public EventDto event(Event v) {
        return new EventDto(v.getId(), v.getName(), v.getMinAge(), v.getDescription());
    }

    public VenueDto venue(Venue v) {
        return new VenueDto(v.getId(), v.getName(), v.getCapacity(), address(v.getAddress()));
    }

    public TicketDto ticket(Ticket v) {
        return new TicketDto(v.getId(), v.getName(), coordinates(v.getCoordinates()), v.getCreationDate(),
                person(v.getPerson()), event(v.getEvent()), v.getPrice(), v.getType(), v.getDiscount(), v.getNumber(),
                venue(v.getVenue()), v.getVersion());
    }

    public ImportOperationDto importOperation(ImportOperation operation) {
        return new ImportOperationDto(operation.getId(), operation.getStatus(), operation.getUsername(),
                operation.getFileName(), operation.getCreatedAt(), operation.getCompletedAt(),
                operation.getImportedCount(), operation.getErrorMessage());
    }

    public TicketCommand command(TicketRequest v) {
        return new TicketCommand(v.name(), new CoordinatesCommand(v.coordinates().x(), v.coordinates().y()),
                v.personId(), v.eventId(), v.price(), v.type(), v.discount(), v.number(), v.venueId());
    }
    
    public PersonCommand command(PersonDto v) {
        return new PersonCommand(v.eyeColor(), v.hairColor(),
                new LocationCommand(v.location().x(), v.location().y(), v.location().name()), v.weight(),
                v.passportID(), v.nationality());
    }

    public EventCommand command(EventDto v) {
        return new EventCommand(v.name(), v.minAge(), v.description());
    }

    public VenueCommand command(VenueDto v) {
        return new VenueCommand(v.name(), v.capacity(),
                new AddressCommand(v.address().street(), v.address().zipCode()));
    }
    private CoordinatesDto coordinates(Coordinates v) {
        return new CoordinatesDto(v.getX(), v.getY());
    }

    private LocationDto location(Location v) {
        return new LocationDto(v.getX(), v.getY(), v.getName());
    }

    private AddressDto address(Address v) {
        return new AddressDto(v.getStreet(), v.getZipCode());
    }
}
