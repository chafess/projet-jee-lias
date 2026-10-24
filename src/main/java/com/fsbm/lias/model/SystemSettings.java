package com.fsbm.lias.model;

import jakarta.persistence.*;

@Entity
public class SystemSettings {
    @Id
    private Long id = 1L; // Toujours l'ID 1

    // Serveur
    private String uploadPath = "C:/lias_uploads/";
    private int maxStorage = 50;

    // Sécurité
    private int maxLoginAttempts = 5;
    private int sessionDuration = 30;
    private boolean doubleAuth = false;

    // Modules (Toggles)
    private boolean modulePubs = true;
    private boolean moduleMateriel = true;
    private boolean moduleEvents = true;
    private boolean moduleFeed = true;
    private boolean moduleAnnuaire = true;

    // Getters and Setters ...
    public Long getId() { return id; }
    public String getUploadPath() { return uploadPath; }
    public void setUploadPath(String uploadPath) { this.uploadPath = uploadPath; }
    public int getMaxStorage() { return maxStorage; }
    public void setMaxStorage(int maxStorage) { this.maxStorage = maxStorage; }
    public int getMaxLoginAttempts() { return maxLoginAttempts; }
    public void setMaxLoginAttempts(int maxLoginAttempts) { this.maxLoginAttempts = maxLoginAttempts; }
    public int getSessionDuration() { return sessionDuration; }
    public void setSessionDuration(int sessionDuration) { this.sessionDuration = sessionDuration; }
    public boolean isDoubleAuth() { return doubleAuth; }
    public void setDoubleAuth(boolean doubleAuth) { this.doubleAuth = doubleAuth; }
    public boolean isModulePubs() { return modulePubs; }
    public void setModulePubs(boolean modulePubs) { this.modulePubs = modulePubs; }
    public boolean isModuleMateriel() { return moduleMateriel; }
    public void setModuleMateriel(boolean moduleMateriel) { this.moduleMateriel = moduleMateriel; }
    public boolean isModuleEvents() { return moduleEvents; }
    public void setModuleEvents(boolean moduleEvents) { this.moduleEvents = moduleEvents; }
    public boolean isModuleFeed() { return moduleFeed; }
    public void setModuleFeed(boolean moduleFeed) { this.moduleFeed = moduleFeed; }
    public boolean isModuleAnnuaire() { return moduleAnnuaire; }
    public void setModuleAnnuaire(boolean moduleAnnuaire) { this.moduleAnnuaire = moduleAnnuaire; }

    // Dans SystemSettings.java
    private int retentionDays = 30;

    public int getRetentionDays() { return retentionDays; }
    public void setRetentionDays(int retentionDays) { this.retentionDays = retentionDays; }
}