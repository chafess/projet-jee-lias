package com.fsbm.lias.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Discussion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subject;
    private LocalDateTime dateCreation;

    @ManyToOne
    private Event event; // null si ce n'est pas un fil d'événement

    // NOUVEAUX CHAMPS
    private String scope; // "EVENT", "TEAM", "GENERAL"

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team; // Rempli uniquement si scope = "TEAM"

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
}