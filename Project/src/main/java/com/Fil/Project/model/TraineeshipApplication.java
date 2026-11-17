package com.Fil.Project.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "traineeship_applications")
public class TraineeshipApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    private Long traineeshipId;   // would reference Traineeship entity

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private LocalDateTime submittedAt = LocalDateTime.now();

    public enum Status { PENDING, APPROVED, REJECTED }

    /* getters/setters */
    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Long getTraineeshipId() { return traineeshipId; }
    public void setTraineeshipId(Long traineeshipId) { this.traineeshipId = traineeshipId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}