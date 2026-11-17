package com.Fil.Project.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="company_evaluations")
public class CompanyEvaluation {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="company_id")
    private Company company;

    private Long positionId;
    private int studentMotivation, studentEffectiveness, studentEfficiency;
    private LocalDateTime createdAt = LocalDateTime.now();

    /* getters/setters */
    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public void setCompany(Company c) { company = c; }
    public Long getPositionId() { return positionId; }
    public void setPositionId(Long p) { positionId = p; }
    public int getStudentMotivation() { return studentMotivation; }
    public void setStudentMotivation(int v) { studentMotivation = v; }
    public int getStudentEffectiveness() { return studentEffectiveness; }
    public void setStudentEffectiveness(int v) { studentEffectiveness = v; }
    public int getStudentEfficiency() { return studentEfficiency; }
    public void setStudentEfficiency(int v) { studentEfficiency = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}