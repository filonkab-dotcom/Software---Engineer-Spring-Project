package com.Fil.Project.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "traineeship_evaluations")
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "professor_id")
    private Professor professor;

    private Long traineeshipPositionId;

    /* Student ratings */
    private int studentMotivation;
    private int studentEffectiveness;
    private int studentEfficiency;

    /* Company ratings */
    private int companyFacilities;
    private int companyGuidance;

    private LocalDateTime createdAt = LocalDateTime.now();

    /* getters/setters */
    public Long getId() { return id; }
    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }
    public Long getTraineeshipPositionId() { return traineeshipPositionId; }
    public void setTraineeshipPositionId(Long traineeshipPositionId) { this.traineeshipPositionId = traineeshipPositionId; }
    public int getStudentMotivation() { return studentMotivation; }
    public void setStudentMotivation(int studentMotivation) { this.studentMotivation = studentMotivation; }
    public int getStudentEffectiveness() { return studentEffectiveness; }
    public void setStudentEffectiveness(int studentEffectiveness) { this.studentEffectiveness = studentEffectiveness; }
    public int getStudentEfficiency() { return studentEfficiency; }
    public void setStudentEfficiency(int studentEfficiency) { this.studentEfficiency = studentEfficiency; }
    public int getCompanyFacilities() { return companyFacilities; }
    public void setCompanyFacilities(int companyFacilities) { this.companyFacilities = companyFacilities; }
    public int getCompanyGuidance() { return companyGuidance; }
    public void setCompanyGuidance(int companyGuidance) { this.companyGuidance = companyGuidance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}