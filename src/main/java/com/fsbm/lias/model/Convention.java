package com.fsbm.lias.model;


import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Convention {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String partnerName; // ex: Microsoft
    private String title; // ex: Azure Cloud Research Grant
    private String type; // INDUSTRIAL or ACADEMIC
    private String location; // ex: Redmond, USA
    private LocalDate startDate;
    private LocalDate endDate;
    private Double budget;
    private String pvPath; // Le fichier PDF du contrat

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team; // Liaison avec l'équipe du labo

    // --- GETTERS ET SETTERS (Alt + Insert) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }
    public String getPvPath() { return pvPath; }
    public void setPvPath(String pvPath) { this.pvPath = pvPath; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
}