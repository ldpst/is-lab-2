package com.ldpst.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ldpst.model.domain.*;
import java.time.Instant;
import java.util.List;

public final class ApiModel {
    public ApiModel() {}

    public record CoordinatesDto(long x, @NotNull Long y) {}

    public record LocationDto(@NotNull Double x, @NotNull Float y, @NotBlank String name) {}

    public record AddressDto(String street, @NotNull @Size(min=7) String zipCode) {}

    public record PersonDto(Long id, Color eyeColor, @NotNull Color hairColor, @Valid @NotNull LocationDto location,
                            @NotNull @Positive Integer weight, @JsonProperty("passportId") @Size(max=26) String passportID, Country nationality) {}

    public record EventDto(Long id, @NotBlank String name, int minAge, @NotBlank String description) {}

    public record VenueDto(Long id, @NotBlank String name, @Positive int capacity, @Valid @NotNull AddressDto address) {}

    public record TicketDto(Integer id, @NotBlank String name, @Valid @NotNull CoordinatesDto coordinates,
                            Instant creationDate, @Valid @NotNull PersonDto person, @Valid @NotNull EventDto event,
                            @Positive float price, @NotNull TicketType type,
                            @Positive @DecimalMax("100") Float discount, @Positive Integer number,
                            @Valid @NotNull VenueDto venue, long version) {}

    public record TicketRequest(@NotBlank String name, @Valid @NotNull CoordinatesDto coordinates,
                                @NotNull @Positive Long personId, @NotNull @Positive Long eventId,
                                @Positive float price, @NotNull TicketType type,
                                @Positive @DecimalMax("100") Float discount, @Positive Integer number,
                                @NotNull @Positive Long venueId, @PositiveOrZero Long version) {}

    public record PageDto<T>(List<T> items, long totalItems, int totalPages, int page, int size) {}

    public record DeleteResultDto(long deletedCount) {}

    public record CountResultDto(long count) {}

    public record ImportOperationDto(Long id, ImportStatus status, String username, String fileName,
                                     Instant createdAt, Instant completedAt, Integer importedCount,
                                     String errorMessage) {}

    public record ImportResultDto(long operationId) {}
    
    public record ApiError(String message, List<String> details) {}
}
    

