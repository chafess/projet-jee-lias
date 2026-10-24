package com.fsbm.lias.model;

import jakarta.persistence.*; // Cette ligne est indispensable !

@Entity
public class Publication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String authors;
    @Column(name = "publication_year")
    private int year;
    private String type;
    private String link;
    private String coAuthors; // ex: "Dr. Sarah Chen, Chaimaa Afess"
    private String journal;   // ex: "IEEE Transactions on Medical Imaging"
    private String doi;

    // --- AJOUTÉ POUR LE DASHBOARD MEMBRE ---
    private String status; // ex: "Published", "Under Review"

    public Long getId() {
        return id;
    }

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member author; // Le nom "author" doit correspondre au "mappedBy" de Member.java

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    private String abstractText; // Le grand champ "Abstract / Description"
    private String volume;       // Le champ "Volume"

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getCoAuthors() {
        return coAuthors;
    }

    public void setCoAuthors(String coAuthors) {
        this.coAuthors = coAuthors;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public Member getAuthor() {
        return author;
    }

    public void setAuthor(Member author) {
        this.author = author;
    }

    // --- GETTER ET SETTER POUR STATUS (AJOUTÉS) ---
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}