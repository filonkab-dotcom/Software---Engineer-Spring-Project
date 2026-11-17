package com.Fil.Project.service;

import com.Fil.Project.model.Evaluation;
import com.Fil.Project.model.TraineeshipPosition;
import java.util.List;

public interface EvaluationRepositoryHolder {
    List<Evaluation> findProfessorEvals(Long positionId);
    long countSupervised(Long professorId);
    void savePosition(TraineeshipPosition tp);
}
