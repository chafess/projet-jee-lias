package com.fsbm.lias.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private Member recipient; // Le destinataire qui reçoit la notification

    private String title;

    @Column(length = 1000)
    private String message;

    private String type; // "EVENT", "VALIDATION", "DOCUMENT"

    private boolean isRead = false; // Non lue par défaut

    private LocalDateTime dateCreation;

    public Notification() {}

    public Notification(Member recipient, String title, String message, String type) {
        this.recipient = recipient;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = false;
        this.dateCreation = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Member getRecipient() { return recipient; }
    public void setRecipient(Member recipient) { this.recipient = recipient; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}