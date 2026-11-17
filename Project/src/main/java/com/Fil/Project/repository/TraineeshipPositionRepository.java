package com.Fil.Project.repository;

import com.Fil.Project.model.TraineeshipPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TraineeshipPositionRepository extends JpaRepository<TraineeshipPosition, Long> {
    List<TraineeshipPosition> findByProfessor_Id(Long professorId);
    long countByProfessor_Id(Long professorId);
}