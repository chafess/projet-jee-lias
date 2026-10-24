package com.fsbm.lias.model;

import jakarta.persistence.*;

@Entity
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member user; // Le nom "user" doit correspondre au "mappedBy" de Member.java
    private String reference; // Numéro de série
    
     // C'est cette variable que le Controller cherche

    // --- GETTERS ET SETTERS (Indispensables !) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Member getUser() { return user; } // La méthode manquante
    public void setUser(Member user) { this.user = user; }
}