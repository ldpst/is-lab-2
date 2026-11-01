package com.ldpst.model.domain;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @Min(value = 1)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long version;

    public long getVersion() {
        return version;
    }

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Valid
    @NotNull
    @Embedded
    private Coordinates coordinates;

    @CreationTimestamp
    @Column(name = "creation_date", nullable = false, updatable = false)
    private Instant creationDate;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "person_id")
    private Person person;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    @Positive
    @Column(nullable = false)
    private float price;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type", nullable = false)
    private TicketType type;

    @Positive
    @DecimalMax("100")
    private Float discount;

    @Positive
    private Integer number;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    public Integer getId() {
        return id;
    }

    public void setId(Integer v) {
        id = v;
    }

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates v) {
        coordinates = v;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant v) {
        creationDate = v;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person v) {
        person = v;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event v) {
        event = v;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float v) {
        price = v;
    }

    public TicketType getType() {
        return type;
    }

    public void setType(TicketType v) {
        type = v;
    }

    public Float getDiscount() {
        return discount;
    }

    public void setDiscount(Float v) {
        discount = v;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer v) {
        number = v;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue v) {
        venue = v;
    }
}
