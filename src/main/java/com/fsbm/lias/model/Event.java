package com.fsbm.lias.model;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String type; // Conference, Seminar, Workshop
    private LocalDate date;
    private String location;

    @ManyToOne
    private Member organizer; // The member responsible

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Member getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Member organizer) {
        this.organizer = organizer;
    }
    private String lieu;
    private boolean ouvertAuxAssocies = false; // Par défaut, un événement est interne

    // Getters et Setters
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public boolean isOuvertAuxAssocies() { return ouvertAuxAssocies; }
    public void setOuvertAuxAssocies(boolean ouvertAuxAssocies) { this.ouvertAuxAssocies = ouvertAuxAssocies; }


    // Dans Event.java
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
}
