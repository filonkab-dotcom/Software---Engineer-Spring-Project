package com.Fil.Project.repository;

import com.Fil.Project.model.TraineeshipApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TraineeshipApplicationRepository extends JpaRepository<TraineeshipApplication, Long> {
    List<TraineeshipApplication> findByStudentId(Long studentId);
    List<TraineeshipApplication> findByStudent_IdAndStatus(Long studentId, TraineeshipApplication.Status status);
    boolean existsByStudentIdAndStatus(Long studentId, TraineeshipApplication.Status status);
}