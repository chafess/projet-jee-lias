package com.fsbm.lias.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String type;   // Compte, Sécurité, Sauvegarde, Mandat
    private String action; // ex: "Rôle modifié -> Chercheur senior"
    private String acteur; // ex: "Admin" ou "Système"
    private String cible;  // ex: "K. Ouali" ou "user@example.com"

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getActeur() { return acteur; }
    public void setActeur(String acteur) { this.acteur = acteur; }
    public String getCible() { return cible; }
    public void setCible(String cible) { this.cible = cible; }
}