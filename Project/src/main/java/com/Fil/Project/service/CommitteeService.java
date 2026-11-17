package com.Fil.Project.service;

import com.Fil.Project.model.CompanyEvaluation;
import com.Fil.Project.model.CompanyPosition;
import com.Fil.Project.model.Evaluation;
import com.Fil.Project.model.Professor;
import com.Fil.Project.model.Student;
import com.Fil.Project.model.TraineeshipApplication;
import com.Fil.Project.model.TraineeshipPosition;
import com.Fil.Project.repository.CompanyEvaluationRepository;
import com.Fil.Project.repository.CompanyPositionRepository;
import com.Fil.Project.repository.ProfessorRepository;
import com.Fil.Project.repository.StudentRepository;
import com.Fil.Project.repository.TraineeshipApplicationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CommitteeService {
    private final TraineeshipApplicationRepository appRepo;
    private final StudentRepository studentRepo;
    private final CompanyPositionRepository posRepo;
    private final CompanyEvaluationRepository compEvalRepo;
    private final ProfessorRepository profRepo;
    private final EvaluationRepositoryHolder evalHolder;

    @Autowired
    public CommitteeService(TraineeshipApplicationRepository appRepo,
                            StudentRepository studentRepo,
                            CompanyPositionRepository posRepo,
                            CompanyEvaluationRepository compEvalRepo,
                            ProfessorRepository profRepo,
                            EvaluationRepositoryHolder evalHolder) {
        this.appRepo = appRepo;
        this.studentRepo = studentRepo;
        this.posRepo = posRepo;
        this.compEvalRepo = compEvalRepo;
        this.profRepo = profRepo;
        this.evalHolder = evalHolder;
    }

    /* ---------- US16: λίστα αιτήσεων ---------- */
    public List<TraineeshipApplication> listStudentApplications() {
        return appRepo.findAll();
    }

    /* ---------- US17: διαθέσιμες θέσεις που ταιριάζουν ---------- */
    public List<CompanyPosition> findMatchingPositions(Long studentId, boolean byInterest, boolean byLocation) {
        Student s = studentRepo.findById(studentId).orElseThrow();
        return posRepo.findByStatus(CompanyPosition.Status.AVAILABLE).stream()
                .filter(p -> s.getSkills().stream().anyMatch(p.getRequiredSkills()::contains))
                .filter(p -> !byInterest || s.getInterests().stream().anyMatch(p.getTopics()::contains))
                .filter(p -> !byLocation || s.getPreferredLocation()==null ||
                        s.getPreferredLocation().equalsIgnoreCase(p.getCompany().getLocation()))
                .collect(Collectors.toList());
    }

    /* ---------- US18: ανάθεση θέσης ---------- */
    @Transactional
    public void assignPosition(Long applicationId, Long positionId) {
        TraineeshipApplication app = appRepo.findById(applicationId).orElseThrow();
        CompanyPosition pos = posRepo.findById(positionId).orElseThrow();
        if(pos.getStatus()!=CompanyPosition.Status.AVAILABLE) throw new IllegalStateException("Position not available");
        pos.setAssignedStudentId(app.getStudent().getId());
        pos.setStatus(CompanyPosition.Status.ASSIGNED);
        posRepo.save(pos);
        app.setStatus(TraineeshipApplication.Status.APPROVED);
        appRepo.save(app);
    }

    /* ---------- US19: ανάθεση επιβλέποντα καθηγητή ---------- */
    @Transactional
    public void assignProfessor(Long positionId) {
        CompanyPosition pos = posRepo.findById(positionId).orElseThrow();
        List<Professor> profs = profRepo.findAll();
        Professor chosen = profs.stream()
                .filter(p -> p.getInterests().stream().anyMatch(pos.getTopics()::contains))
                .min(Comparator.comparingLong(p -> evalHolder.countSupervised(p.getId())))
                .orElseGet(() -> profs.stream().min(Comparator.comparingLong(p -> evalHolder.countSupervised(p.getId()))).orElseThrow());
        TraineeshipPosition tp = new TraineeshipPosition();
        tp.setProfessor(chosen);
        tp.setTitle(pos.getDescription());
        evalHolder.savePosition(tp);
    }

    /* ---------- US20: πρακτικές σε εξέλιξη ---------- */
    public List<CompanyPosition> listInProgress() {
        return posRepo.findByStatus(CompanyPosition.Status.ASSIGNED);
    }

    /* ---------- US21: οριστικοποίηση πρακτικής ---------- */
    @Transactional
    public boolean finalizeTraineeship(Long positionId) {
        double avg = Stream.concat(
                        evalHolder.findProfessorEvals(positionId).stream()
                                .mapToInt(e -> (e.getStudentMotivation()+e.getStudentEffectiveness()+e.getStudentEfficiency())/3)
                                .boxed(),
                        compEvalRepo.findByPositionId(positionId).stream()
                                .mapToInt(e -> (e.getStudentMotivation()+e.getStudentEffectiveness()+e.getStudentEfficiency())/3)
                                .boxed())
                .mapToInt(Integer::intValue)
                .average().orElse(0.0);

        CompanyPosition pos = posRepo.findById(positionId).orElseThrow();
        pos.setStatus(CompanyPosition.Status.DELETED);
        posRepo.save(pos);
        return avg>=3.0;
    }
}