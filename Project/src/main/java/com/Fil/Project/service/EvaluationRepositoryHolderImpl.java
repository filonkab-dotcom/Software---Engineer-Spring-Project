package com.Fil.Project.service;

import com.Fil.Project.model.Evaluation;
import com.Fil.Project.model.TraineeshipPosition;
import com.Fil.Project.repository.EvaluationRepository;
import com.Fil.Project.repository.TraineeshipPositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EvaluationRepositoryHolderImpl implements EvaluationRepositoryHolder {

    private final EvaluationRepository evalRepo;
    private final TraineeshipPositionRepository tpRepo;

    @Autowired
    public EvaluationRepositoryHolderImpl(
            EvaluationRepository evalRepo,
            TraineeshipPositionRepository tpRepo) {
        this.evalRepo = evalRepo;
        this.tpRepo = tpRepo;
    }

    @Override
    public List<Evaluation> findProfessorEvals(Long posId) {
        return evalRepo.findByTraineeshipPositionId(posId);
    }

    @Override
    public long countSupervised(Long professorId) {
        return tpRepo.countByProfessor_Id(professorId);
    }

    @Override
    public void savePosition(TraineeshipPosition tp) {
        tpRepo.save(tp);
    }
}