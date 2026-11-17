package com.Fil.Project.model;

import jakarta.persistence.*;

@Entity
@Table(name = "traineeship_positions")
public class TraineeshipPosition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String status = "ACTIVE";

    @ManyToOne(optional = false)
    @JoinColumn(name = "professor_id")
    private Professor professor;

    /* getters/setters */
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}