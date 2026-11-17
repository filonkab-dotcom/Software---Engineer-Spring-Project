package com.Fil.Project.repository;

import com.Fil.Project.model.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    long countByTraineeshipPositionId(Long traineeshipPositionId);
    List<Evaluation> findByTraineeshipPositionId(Long traineeshipPositionId);
    List<Evaluation> findByTraineeshipPositionIdAndProfessorId(Long traineeshipPositionId, Long professorId);
}