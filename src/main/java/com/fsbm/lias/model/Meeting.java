package com.fsbm.lias.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Meeting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // L'objet principal (ex: Budget 2026 Allocation)
    private String description; // Le sous-texte (ex: New equipment purchases)
    private LocalDateTime dateTime;
    private String status = "Scheduled"; // Scheduled or Completed
    private String pvPath; // Chemin vers le fichier PDF du PV

    // --- Getters et Setters (Alt + Insert) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPvPath() { return pvPath; }
    public void setPvPath(String pvPath) { this.pvPath = pvPath; }
}