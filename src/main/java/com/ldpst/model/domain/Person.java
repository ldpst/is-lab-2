package com.ldpst.model.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "persons")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "eye_color")
    private Color eyeColor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "hair_color", nullable = false)
    private Color hairColor;

    @Valid
    @NotNull
    @Embedded
    private Location location;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer weight;

    @Size(max = 26)
    @Column(name = "passport_id", length = 26)
    private String passportID;

    @Enumerated(EnumType.STRING)
    private Country nationality;

    @OneToMany(mappedBy = "person", cascade = CascadeType.REMOVE)
    private List<Ticket> tickets = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Color getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(Color v) {
        eyeColor = v;
    }

    public Color getHairColor() {
        return hairColor;
    }

    public void setHairColor(Color v) {
        hairColor = v;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location v) {
        location = v;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer v) {
        weight = v;
    }

    public String getPassportID() {
        return passportID;
    }

    public void setPassportID(String v) {
        passportID = v;
    }

    public Country getNationality() {
        return nationality;
    }

    public void setNationality(Country v) {
        nationality = v;
    }
}
