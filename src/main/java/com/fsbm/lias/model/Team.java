package com.fsbm.lias.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String researchAxe;

    @OneToMany(mappedBy = "team")
    private List<Member> members;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResearchAxe() {
        return researchAxe;
    }

    public void setResearchAxe(String researchAxe) {
        this.researchAxe = researchAxe;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }
// --- ICI : FAIS CLIC DROIT -> GENERATE -> GETTER AND SETTER ---
    // Sélectionne tout et valide.
}