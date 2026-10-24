package com.fsbm.lias.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Actualite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    private String type; // ex: Publication, Événement, Soutenance, Recrutement, Convention

    private LocalDateTime dateCreation;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member auteur; // Le directeur qui publie

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public Member getAuteur() { return auteur; }
    public void setAuteur(Member auteur) { this.auteur = auteur; }
    // Dans Actualite.java
    private String documentPath;

    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
}