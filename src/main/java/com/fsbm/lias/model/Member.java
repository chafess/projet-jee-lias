package com.fsbm.lias.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    // Rôles
    public static final String ROLE_ADMIN = "ADMINISTRATEUR";
    public static final String ROLE_DIRECTOR = "DIRECTEUR";
    public static final String ROLE_VICE_DIRECTOR = "VICE_DIRECTEUR";
    public static final String ROLE_TEAM_LEADER = "CHEF_EQUIPE";
    public static final String ROLE_PERMANENT = "MEMBRE_PERMANENT";
    public static final String ROLE_ASSOCIATE = "MEMBRE_ASSOCIE";
    public static final String ROLE_PHD = "DOCTORANT";
    public static final String ROLE_RETIRED = "RETRAITE";
    public static final String ROLE_FORMER = "ANCIEN_MEMBRE";
    // Ajoutez ceci dans votre classe Member
    private boolean canPublish = false;

    // Ajoutez les getters/setters correspondants
    public boolean isCanPublish() { return canPublish; }
    public void setCanPublish(boolean canPublish) { this.canPublish = canPublish; }

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private String role;
    private String status;

    private String photoPath;
    private String cvPath;

    private String etablissementOrigine;
    private String laboratoireOrigine;

    @Column(length = 1000)
    private String biographie;
    private String centresInteret;

    private LocalDate dateNaissance;
    private LocalDate dateEmbauche;
    private LocalDate dateAffiliation;

    private boolean active = false;

    // --- RELATIONS ---

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "author")
    private List<Publication> publications;

    @OneToMany(mappedBy = "user")
    private List<Equipment> equipments;

    // ✅ AJOUTÉ : Relation avec les demandes de matériel
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<MaterialRequest> materialRequests;

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public String getCvPath() { return cvPath; }
    public void setCvPath(String cvPath) { this.cvPath = cvPath; }

    public String getEtablissementOrigine() { return etablissementOrigine; }
    public void setEtablissementOrigine(String etablissementOrigine) { this.etablissementOrigine = etablissementOrigine; }

    public String getLaboratoireOrigine() { return laboratoireOrigine; }
    public void setLaboratoireOrigine(String laboratoireOrigine) { this.laboratoireOrigine = laboratoireOrigine; }

    public String getBiographie() { return biographie; }
    public void setBiographie(String biographie) { this.biographie = biographie; }

    public String getCentresInteret() { return centresInteret; }
    public void setCentresInteret(String centresInteret) { this.centresInteret = centresInteret; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public LocalDate getDateEmbauche() { return dateEmbauche; }
    public void setDateEmbauche(LocalDate dateEmbauche) { this.dateEmbauche = dateEmbauche; }

    public LocalDate getDateAffiliation() { return dateAffiliation; }
    public void setDateAffiliation(LocalDate dateAffiliation) { this.dateAffiliation = dateAffiliation; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public List<Publication> getPublications() { return publications; }
    public void setPublications(List<Publication> publications) { this.publications = publications; }

    public List<Equipment> getEquipments() { return equipments; }
    public void setEquipments(List<Equipment> equipments) { this.equipments = equipments; }

    // ✅ AJOUTÉ : Getter et Setter pour les demandes de matériel
    public List<MaterialRequest> getMaterialRequests() { return materialRequests; }
    public void setMaterialRequests(List<MaterialRequest> materialRequests) { this.materialRequests = materialRequests; }
    // Ajoutez ceci dans Member.java
    @ManyToMany
    @JoinTable(
            name = "member_event_participation",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private List<Event> participatingEvents;

    // Ajoutez les getters et setters
    public List<Event> getParticipatingEvents() { return participatingEvents; }
    public void setParticipatingEvents(List<Event> participatingEvents) { this.participatingEvents = participatingEvents; }
    // Ajoutez ce champ dans votre classe Member
    private String directeurThese;

    // Ajoutez les getters et setters
    public String getDirecteurThese() { return directeurThese; }
    public void setDirecteurThese(String directeurThese) { this.directeurThese = directeurThese; }
}