package com.Fil.Project.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "logbook_entries")
public class LogbookEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id")
    private TraineeshipApplication application;

    private LocalDate entryDate;
    private boolean editable = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    /* getters/setters */
    public Long getId() { return id; }
    public TraineeshipApplication getApplication() { return application; }
    public void setApplication(TraineeshipApplication application) { this.application = application; }
    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isEditable() { return editable; }
    public void setEditable(boolean editable) { this.editable = editable; }
}