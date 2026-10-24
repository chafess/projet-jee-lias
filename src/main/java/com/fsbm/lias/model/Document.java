package com.fsbm.lias.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String type;
    private String filePath;

    // ✅ AJOUTEZ CETTE LIGNE (Variable manquante)
    /*private int year;*/

    @ManyToOne
    @JoinColumn(name = "uploader_id")
    private Member uploader;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
    @ManyToOne
    private Event event; // Lien vers l'événement (pour filtrer par événement)

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    // ✅ AJOUTEZ CES MÉTHODES (Méthodes manquantes)
    /*public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }*/
    //
    private Integer year;
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
     //

    public Member getUploader() { return uploader; }
    public void setUploader(Member uploader) { this.uploader = uploader; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
    // Ajoutez ceci dans Document.java
    @ManyToOne
    /*@JoinColumn(name = "event_id")
    private Event event;*/

    // Ajoutez les getters et setters
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
}