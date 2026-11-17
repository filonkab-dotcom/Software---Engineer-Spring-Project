package com.Fil.Project.controller;

import com.Fil.Project.dto.EvaluationRequest;
import com.Fil.Project.dto.ProfessorProfileRequest;
import com.Fil.Project.model.Evaluation;
import com.Fil.Project.model.Professor;
import com.Fil.Project.model.TraineeshipPosition;
import com.Fil.Project.repository.EvaluationRepository;
import com.Fil.Project.repository.ProfessorRepository;
import com.Fil.Project.repository.TraineeshipPositionRepository;
import com.Fil.Project.service.ProfessorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller για τη διαχείριση των λειτουργιών του καθηγητή.
 */
@Controller
@RequestMapping("/professor")
public class ProfessorController {

    private final ProfessorService professorService;
    private final ProfessorRepository professorRepository;
    private final TraineeshipPositionRepository positionRepository;
    private final EvaluationRepository evaluationRepository;

    @Autowired
    public ProfessorController(ProfessorService professorService,
                               ProfessorRepository professorRepository,
                               TraineeshipPositionRepository positionRepository,
                               EvaluationRepository evaluationRepository) {
        this.professorService = professorService;
        this.professorRepository = professorRepository;
        this.positionRepository = positionRepository;
        this.evaluationRepository = evaluationRepository;
    }

    /**
     * Επιστρέφει τον τρέχοντα συνδεδεμένο καθηγητή ή null αν δεν είναι καθηγητής.
     */
    private Professor getCurrentProfessor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof Professor) {
                return (Professor) principal;
            }
        }
        return null;
    }

    /**
     * Εμφανίζει τη σελίδα προφίλ του καθηγητή.
     */
    @GetMapping("/profile")
    public String showProfile(Model model) {
        Professor professor = getCurrentProfessor();
        if (professor == null) {
            return "redirect:/login";
        }

        // Δημιουργία αντικειμένου ProfessorProfileRequest για τη φόρμα
        ProfessorProfileRequest professorForm = new ProfessorProfileRequest(
                professor.getFirstName(),
                professor.getLastName(),
                professor.getEmail(),
                professor.getInterests()
        );

        model.addAttribute("professorForm", professorForm);
        model.addAttribute("user", professor);

        // Προσθήκη στατιστικών στοιχείων
        long activePositions = positionRepository.countByProfessor_Id(professor.getId());
        model.addAttribute("activePositions", activePositions);

        long totalPositions = 12; // Υποθετική τιμή
        model.addAttribute("totalPositions", totalPositions);

        return "professor/profile";
    }

    /**
     * Επεξεργάζεται την ενημέρωση του προφίλ καθηγητή.
     */
    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("professorForm") ProfessorProfileRequest professorRequest,
                                BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("user", getCurrentProfessor());
            return "professor/profile";
        }

        try {
            Professor professor = getCurrentProfessor();
            if (professor == null) {
                return "redirect:/login";
            }

            professorService.createOrUpdateProfessor(professor, professorRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Το προφίλ σας ενημερώθηκε επιτυχώς!");
            return "redirect:/professor/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", getCurrentProfessor());
            return "professor/profile";
        }
    }

    /**
     * Εμφανίζει τη λίστα με τις θέσεις πρακτικής που επιβλέπει ο καθηγητής.
     */
    @GetMapping("/positions")
    public String listPositions(Model model) {
        Professor professor = getCurrentProfessor();
        if (professor == null) {
            return "redirect:/login";
        }

        List<TraineeshipPosition> positions = professorService.listSupervisedPositions(professor.getId());

        // Εμπλουτισμός των θέσεων με επιπλέον πληροφορίες
        int activeCount = 0;
        int pendingEvaluationCount = 0;
        int completedCount = 0;

        for (TraineeshipPosition position : positions) {
            // Έλεγχος αξιολογήσεων
            long evaluationCount = evaluationRepository.countByTraineeshipPositionId(position.getId());

            // Για απλότητα, χρησιμοποιούμε enum για κατάσταση θέσης
            // Στην πραγματικότητα, μπορεί να χρειάζεται πιο περίπλοκη λογική
            if (evaluationCount > 0) {
                position.setStatus("COMPLETED");
                completedCount++;
            } else {
                // Υποθετικά, χρειάζεται αξιολόγηση αν η θέση είναι ενεργή και δεν έχει αξιολογηθεί
                position.setStatus("PENDING_EVALUATION");
                pendingEvaluationCount++;
            }

            if ("ACTIVE".equals(position.getStatus())) {
                activeCount++;
            }
        }

        model.addAttribute("positions", positions);
        model.addAttribute("user", professor);
        model.addAttribute("activePositionsCount", activeCount);
        model.addAttribute("pendingEvaluationCount", pendingEvaluationCount);
        model.addAttribute("completedPositionsCount", completedCount);

        return "professor/positions";
    }

    /**
     * Εμφανίζει τις λεπτομέρειες μιας θέσης πρακτικής.
     */
    @GetMapping("/positions/{id}")
    public String showPosition(@PathVariable("id") Long positionId, Model model) {
        Professor professor = getCurrentProfessor();
        if (professor == null) {
            return "redirect:/login";
        }

        Optional<TraineeshipPosition> optionalPosition = positionRepository.findById(positionId);
        if (optionalPosition.isEmpty() || !optionalPosition.get().getProfessor().getId().equals(professor.getId())) {
            return "redirect:/professor/positions";
        }

        TraineeshipPosition position = optionalPosition.get();
        model.addAttribute("position", position);
        model.addAttribute("user", professor);

        // Φόρτωση αξιολογήσεων
        List<Evaluation> evaluations = evaluationRepository.findByTraineeshipPositionId(positionId);
        model.addAttribute("evaluations", evaluations);

        return "professor/position-details";
    }

    /**
     * Εμφανίζει τη φόρμα αξιολόγησης για μια θέση πρακτικής.
     */
    @GetMapping("/evaluate/{id}")
    public String showEvaluationForm(@PathVariable("id") Long positionId, Model model) {
        Professor professor = getCurrentProfessor();
        if (professor == null) {
            return "redirect:/login";
        }

        Optional<TraineeshipPosition> optionalPosition = positionRepository.findById(positionId);
        if (optionalPosition.isEmpty() || !optionalPosition.get().getProfessor().getId().equals(professor.getId())) {
            return "redirect:/professor/positions";
        }

        TraineeshipPosition position = optionalPosition.get();
        model.addAttribute("position", position);
        model.addAttribute("user", professor);

        // Δημιουργία αντικειμένου φοιτητή για την προβολή (θα πρέπει να φορτωθεί από τη βάση)
        Map<String, Object> student = new HashMap<>();
        student.put("firstName", "Γιώργος");
        student.put("lastName", "Παπαδόπουλος");
        student.put("universityId", "ΑΜ12345");
        student.put("email", "g.papadopoulos@example.com");
        model.addAttribute("student", student);

        // Έλεγχος για προηγούμενες αξιολογήσεις
        List<Evaluation> evaluations = evaluationRepository.findByTraineeshipPositionIdAndProfessorId(positionId, professor.getId());
        model.addAttribute("evaluations", evaluations);

        // Δημιουργία νέου αντικειμένου αξιολόγησης για τη φόρμα
        model.addAttribute("evaluationForm", new EvaluationRequest(0, 0, 0, 0, 0));

        return "professor/evaluate";
    }

    /**
     * Επεξεργάζεται την υποβολή αξιολόγησης.
     */
    @PostMapping("/evaluate")
    public String submitEvaluation(@RequestParam Long positionId,
                                   @Valid @ModelAttribute("evaluationForm") EvaluationRequest evaluationRequest,
                                   BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        Professor professor = getCurrentProfessor();
        if (professor == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            Optional<TraineeshipPosition> optionalPosition = positionRepository.findById(positionId);
            if (optionalPosition.isPresent()) {
                model.addAttribute("position", optionalPosition.get());
            }
            model.addAttribute("user", professor);
            return "professor/evaluate";
        }

        try {
            professorService.evaluate(professor.getId(), positionId, evaluationRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Η αξιολόγηση υποβλήθηκε επιτυχώς!");
            return "redirect:/professor/positions";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/professor/evaluate/" + positionId;
        }
    }
}