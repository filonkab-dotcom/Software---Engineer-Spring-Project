package com.Fil.Project.service;

import com.Fil.Project.dto.EvaluationRequest;
import com.Fil.Project.dto.ProfessorProfileRequest;
import com.Fil.Project.model.Evaluation;
import com.Fil.Project.model.Professor;
import com.Fil.Project.model.TraineeshipPosition;
import com.Fil.Project.model.User;
import com.Fil.Project.repository.EvaluationRepository;
import com.Fil.Project.repository.ProfessorRepository;
import com.Fil.Project.repository.TraineeshipPositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProfessorService {
    private final ProfessorRepository professorRepo;
    private final TraineeshipPositionRepository positionRepo;
    private final EvaluationRepository evaluationRepo;

    @Autowired
    public ProfessorService(ProfessorRepository professorRepo,
                            TraineeshipPositionRepository positionRepo,
                            EvaluationRepository evaluationRepo) {
        this.professorRepo = professorRepo;
        this.positionRepo = positionRepo;
        this.evaluationRepo = evaluationRepo;
    }

    /* -------- PRF‑001: Create/Update profile -------- */
    @Transactional
    public Professor createOrUpdateProfessor(User baseUser, ProfessorProfileRequest req) {
        Professor professor = (baseUser instanceof Professor p) ? p : new Professor();
        professor.setId(baseUser.getId());
        professor.setFirstName(baseUser.getFirstName());
        professor.setLastName(baseUser.getLastName());
        professor.setEmail(baseUser.getEmail());
        professor.setPassword(baseUser.getPassword());
        professor.setInterests(req.interests());
        return professorRepo.save(professor);
    }

    /* -------- PRF‑002: List supervised positions -------- */
    public List<TraineeshipPosition> listSupervisedPositions(Long professorId) {
        return positionRepo.findByProfessor_Id(professorId);
    }

    /* -------- PRF‑003: Fill evaluation -------- */
    @Transactional
    public Evaluation evaluate(Long professorId, Long traineeshipPositionId, EvaluationRequest req) {
        Professor professor = professorRepo.findById(professorId)
                .orElseThrow(() -> new IllegalStateException("Professor not found"));

        Optional<TraineeshipPosition> posOpt = positionRepo.findById(traineeshipPositionId);
        if (posOpt.isEmpty() || !posOpt.get().getProfessor().getId().equals(professorId)) {
            throw new IllegalArgumentException("Professor does not supervise this traineeship");
        }

        Evaluation ev = new Evaluation();
        ev.setProfessor(professor);
        ev.setTraineeshipPositionId(traineeshipPositionId);
        ev.setStudentMotivation(req.studentMotivation());
        ev.setStudentEffectiveness(req.studentEffectiveness());
        ev.setStudentEfficiency(req.studentEfficiency());
        ev.setCompanyFacilities(req.companyFacilities());
        ev.setCompanyGuidance(req.companyGuidance());
        return evaluationRepo.save(ev);
    }
}