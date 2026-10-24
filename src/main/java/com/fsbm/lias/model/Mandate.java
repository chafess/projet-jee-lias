/*package com.fsbm.lias.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Mandate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // DIRECTEUR, CHEF_EQUIPE, etc.
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // ACTIF, TERMINE

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team; // Rempli uniquement pour les chefs d'équipe

    // Getters et Setters...
}*/
package com.fsbm.lias.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Mandate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // DIRECTEUR, VICE_DIRECTEUR, CHEF_EQUIPE
    private LocalDate startDate;
    private LocalDate endDate; // null si le mandat est toujours en cours
    private String status; // ACTIF, TERMINE

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team; // rempli uniquement si type = CHEF_EQUIPE

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
}