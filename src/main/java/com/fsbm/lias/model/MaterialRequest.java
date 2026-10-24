package com.fsbm.lias.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class MaterialRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private String description;
    private String requestType; // PURCHASE or RENTAL
    private String status = "PENDING";
    private LocalDate requestDate = LocalDate.now();

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    // --- GETTERS AND SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
}