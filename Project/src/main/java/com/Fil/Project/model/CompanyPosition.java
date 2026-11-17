package com.Fil.Project.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name="company_positions")
public class CompanyPosition {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="company_id")
    private Company company;

    private LocalDate startDate, endDate;

    @Column(columnDefinition="TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name="company_position_skills", joinColumns=@JoinColumn(name="position_id"))
    @Column(name="skill", nullable=false)
    private List<String> requiredSkills;

    @ElementCollection
    @CollectionTable(name="company_position_topics", joinColumns=@JoinColumn(name="position_id"))
    @Column(name="topic", nullable=false)
    private List<String> topics;

    public enum Status{AVAILABLE, ASSIGNED, DELETED}

    @Enumerated(EnumType.STRING)
    private Status status = Status.AVAILABLE;

    private Long assignedStudentId;
    private int professorEvals;
    private int companyEvals;

    /* getters/setters */
    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public void setCompany(Company c) { company = c; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate d) { startDate = d; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate d) { endDate = d; }
    public String getDescription() { return description; }
    public void setDescription(String d) { description = d; }
    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> l) { requiredSkills = l; }
    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> l) { topics = l; }
    public Status getStatus() { return status; }
    public void setStatus(Status s) { status = s; }
    public int getProfessorEvals() { return professorEvals; }
    public void setProfessorEvals(int evals) { professorEvals = evals; }
    public int getCompanyEvals() { return companyEvals; }
    public void setCompanyEvals(int evals) { companyEvals = evals; }
    public Long getAssignedStudentId() { return assignedStudentId; }
    public void setAssignedStudentId(Long id) { assignedStudentId = id; }
}