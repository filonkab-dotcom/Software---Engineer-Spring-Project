package com.Fil.Project.controller;

import com.Fil.Project.model.CompanyPosition;
import com.Fil.Project.model.Evaluation;
import com.Fil.Project.model.Professor;
import com.Fil.Project.model.Student;
import com.Fil.Project.model.TraineeshipApplication;
import com.Fil.Project.model.User;
import com.Fil.Project.repository.EvaluationRepository;
import com.Fil.Project.repository.ProfessorRepository;
import com.Fil.Project.repository.StudentRepository;
import com.Fil.Project.repository.TraineeshipApplicationRepository;
import com.Fil.Project.repository.TraineeshipPositionRepository;
import com.Fil.Project.service.CommitteeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller για τη διαχείριση των λειτουργιών της επιτροπής πρακτικής άσκησης.
 */
@Controller
@RequestMapping("/committee")
public class CommitteeController {

    private final CommitteeService committeeService;
    private final StudentRepository studentRepository;
    private final TraineeshipApplicationRepository applicationRepository;
    private final ProfessorRepository professorRepository;

    @Autowired
    public CommitteeController(CommitteeService committeeService,
                               StudentRepository studentRepository,
                               TraineeshipApplicationRepository applicationRepository,
                               ProfessorRepository professorRepository) {
        this.committeeService = committeeService;
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
        this.professorRepository = professorRepository;
    }

    /**
     * Επιστρέφει τον τρέχοντα συνδεδεμένο χρήστη ή null αν δεν είναι συνδεδεμένος.
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            }
        }
        return null;
    }

    /**
     * Εμφανίζει τη λίστα με τις αιτήσεις φοιτητών (US16).
     */
    @GetMapping("/applications")
    public String listApplications(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        List<TraineeshipApplication> applications = committeeService.listStudentApplications();
        model.addAttribute("applications", applications);
        model.addAttribute("user", user);

        return "committee/applications";
    }

    /**
     * Εμφανίζει τη σελίδα εύρεσης θέσεων για φοιτητή (US17).
     */
    @GetMapping("/matching")
    public String findMatchingPositions(@RequestParam Long studentId,
                                        @RequestParam(required = false) Boolean byInterest,
                                        @RequestParam(required = false) Boolean byLocation,
                                        Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Student> optionalStudent = studentRepository.findById(studentId);
        if (optionalStudent.isEmpty()) {
            return "redirect:/committee/applications";
        }

        Student student = optionalStudent.get();
        model.addAttribute("student", student);
        model.addAttribute("user", user);

        // Αν δεν έχουν οριστεί κριτήρια, εμφάνισε μόνο τη φόρμα
        if (byInterest == null && byLocation == null) {
            model.addAttribute("positions", new ArrayList<>());
            return "committee/matching";
        }

        // Αλλιώς, βρες τις θέσεις που ταιριάζουν
        List<CompanyPosition> positions = committeeService.findMatchingPositions(
                studentId,
                Boolean.TRUE.equals(byInterest),
                Boolean.TRUE.equals(byLocation));

        model.addAttribute("positions", positions);
        model.addAttribute("byInterest", byInterest);
        model.addAttribute("byLocation", byLocation);

        return "committee/matching";
    }

    /**
     * Εμφανίζει τη σελίδα επιβεβαίωσης ανάθεσης θέσης (US18).
     */
    @GetMapping("/assign")
    public String showAssignForm(@RequestParam Long applicationId,
                                 @RequestParam Long positionId,
                                 Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        Optional<TraineeshipApplication> optionalApplication = applicationRepository.findById(applicationId);
        if (optionalApplication.isEmpty()) {
            return "redirect:/committee/applications";
        }

        TraineeshipApplication application = optionalApplication.get();
        CompanyPosition position = null;

        // Εδώ θα φορτώναμε το CompanyPosition με το positionId από το repository
        // Για απλότητα, χρησιμοποιούμε ένα dummy object
        // position = positionRepository.findById(positionId).orElse(null);

        model.addAttribute("application", application);
        model.addAttribute("position", position);
        model.addAttribute("user", user);

        return "committee/assign";
    }

    /**
     * Επεξεργάζεται την ανάθεση θέσης σε φοιτητή (US18).
     */
    @PostMapping("/assign")
    public String assignPosition(@RequestParam Long applicationId,
                                 @RequestParam Long positionId,
                                 RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        try {
            committeeService.assignPosition(applicationId, positionId);
            redirectAttributes.addFlashAttribute("successMessage", "Η θέση πρακτικής ανατέθηκε επιτυχώς στον φοιτητή!");
            return "redirect:/committee/in-progress";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/committee/matching?studentId=" +
                    applicationRepository.findById(applicationId)
                            .map(app -> app.getStudent().getId().toString())
                            .orElse("");
        }
    }

    /**
     * Εμφανίζει τη σελίδα ανάθεσης καθηγητή (US19).
     */
    @GetMapping("/assign-professor")
    public String showAssignProfessorForm(@RequestParam Long positionId, Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        // Εδώ θα φορτώναμε το CompanyPosition με το positionId από το repository
        // Για απλότητα, χρησιμοποιούμε ένα dummy object
        CompanyPosition position = null; // positionRepository.findById(positionId).orElse(null);
        if (position == null) {
            return "redirect:/committee/in-progress";
        }

        // Φόρτωση φοιτητή από το position.getAssignedStudentId()
        Student student = null; // studentRepository.findById(position.getAssignedStudentId()).orElse(null);

        // Φόρτωση όλων των καθηγητών
        List<Professor> professors = professorRepository.findAll();

        // Εύρεση προτεινόμενου καθηγητή βάσει ενδιαφερόντων και φόρτου
        Professor suggestedProfessor = professors.stream()
                .sorted((p1, p2) -> {
                    // Εδώ θα υπήρχε λογική για ταίριασμα βάσει ενδιαφερόντων και φόρτου
                    return 0; // Dummy sorting
                })
                .findFirst()
                .orElse(null);

        // Δημιουργία λίστας με τα ταιριάσματα ενδιαφερόντων για κάθε καθηγητή
        List<List<String>> professorMatches = professors.stream()
                .map(p -> {
                    // Εδώ θα υπήρχε λογική για εύρεση κοινών ενδιαφερόντων
                    return new ArrayList<String>();
                })
                .collect(Collectors.toList());

        // Φόρτος καθηγητών
        List<Long> professorLoads = professors.stream()
                .map(p -> {
                    // Εδώ θα υπήρχε λογική για ανάκτηση φόρτου
                    return 0L;
                })
                .collect(Collectors.toList());

        model.addAttribute("position", position);
        model.addAttribute("student", student);
        model.addAttribute("professors", professors);
        model.addAttribute("suggestedProfessor", suggestedProfessor);
        model.addAttribute("professorMatches", professorMatches);
        model.addAttribute("professorLoads", professorLoads);
        model.addAttribute("user", user);

        return "committee/assign-professor";
    }

    /**
     * Επεξεργάζεται την ανάθεση καθηγητή (US19).
     */
    @PostMapping("/assign-professor")
    public String assignProfessor(@RequestParam Long positionId,
                                  @RequestParam(required = false) Long professorId,
                                  RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        try {
            committeeService.assignProfessor(positionId);
            redirectAttributes.addFlashAttribute("successMessage", "Ο καθηγητής ανατέθηκε επιτυχώς!");
            return "redirect:/committee/in-progress";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/committee/assign-professor?positionId=" + positionId;
        }
    }

    /**
     * Εμφανίζει τη λίστα με τις πρακτικές σε εξέλιξη (US20).
     */
    @GetMapping("/in-progress")
    public String listInProgressTraineeships(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        List<CompanyPosition> positions = committeeService.listInProgress();

        // Εμπλουτισμός των θέσεων με επιπλέον πληροφορίες (π.χ. αξιολογήσεις)
        // Εδώ θα προσθέταμε πληροφορίες για καθηγητές και αξιολογήσεις
        for (CompanyPosition position : positions) {
            // Προσθήκη πληροφοριών για αξιολογήσεις
            // (στην πραγματικότητα, θα φορτώναμε από τη βάση)
            position.setProfessorEvals(0);
            position.setCompanyEvals(0);

            // Φόρτωση στοιχείων φοιτητή και καθηγητή
            // (στην πραγματικότητα, θα χρησιμοποιούσαμε τα repositories)
        }

        // Φόρτωση λιστών για τα φίλτρα
        List<Map<String, Object>> companies = new ArrayList<>();
        List<Map<String, Object>> professors = new ArrayList<>();

        model.addAttribute("positions", positions);
        model.addAttribute("companies", companies);
        model.addAttribute("professors", professors);
        model.addAttribute("user", user);

        return "committee/in-progress";
    }

    /**
     * Εμφανίζει τις λεπτομέρειες μιας πρακτικής άσκησης.
     */
    @GetMapping("/details")
    public String showTraineeshipDetails(@RequestParam Long positionId, Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        // Φόρτωση της θέσης και όλων των σχετικών πληροφοριών
        // Για απλότητα, χρησιμοποιούμε dummy δεδομένα
        CompanyPosition position = null; // positionRepository.findById(positionId).orElse(null);
        if (position == null) {
            return "redirect:/committee/in-progress";
        }

        model.addAttribute("position", position);
        model.addAttribute("user", user);

        return "committee/details";
    }

    /**
     * Εμφανίζει τη σελίδα οριστικοποίησης πρακτικής (US21).
     */
    @GetMapping("/finalize")
    public String showFinalizeForm(@RequestParam Long positionId, Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        // Φόρτωση της θέσης και όλων των σχετικών πληροφοριών
        // Για απλότητα, χρησιμοποιούμε dummy δεδομένα
        CompanyPosition position = null; // positionRepository.findById(positionId).orElse(null);
        if (position == null) {
            return "redirect:/committee/in-progress";
        }

        // Υποθετικός φοιτητής
        Map<String, String> student = new HashMap<>();
        student.put("firstName", "Γιώργος");
        student.put("lastName", "Παπαδόπουλος");
        student.put("universityId", "ΑΜ12345");
        student.put("email", "g.papadopoulos@example.com");

        // Υποθετικός καθηγητής
        Map<String, String> supervisor = new HashMap<>();
        supervisor.put("firstName", "Μαρία");
        supervisor.put("lastName", "Αντωνίου");
        supervisor.put("email", "m.antoniou@example.com");

        // Αξιολογήσεις καθηγητή
        List<Evaluation> professorEvaluations = new ArrayList<>(); // evaluationRepository.findByTraineeshipPositionId(positionId);

        // Αξιολογήσεις εταιρείας
        List<Object> companyEvaluations = new ArrayList<>(); // companyEvaluationRepository.findByPositionId(positionId);

        // Καταχωρίσεις ημερολογίου
        List<Object> logbookEntries = new ArrayList<>(); // logbookEntryRepository.findByApplicationId(...);

        // Υπολογισμός μέσης βαθμολογίας
        double averageScore = 3.8; // Υποθετική τιμή

        model.addAttribute("position", position);
        model.addAttribute("student", student);
        model.addAttribute("supervisor", supervisor);
        model.addAttribute("professorEvaluations", professorEvaluations);
        model.addAttribute("companyEvaluations", companyEvaluations);
        model.addAttribute("logbookEntries", logbookEntries);
        model.addAttribute("averageScore", averageScore);
        model.addAttribute("user", user);

        return "committee/finalize";
    }

    /**
     * Επεξεργάζεται την οριστικοποίηση πρακτικής (US21).
     */
    @PostMapping("/finalize")
    public String finalizeTraineeship(@RequestParam Long positionId,
                                      RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        try {
            boolean success = committeeService.finalizeTraineeship(positionId);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Η πρακτική άσκηση ολοκληρώθηκε επιτυχώς με βαθμολογία που υπερβαίνει τη βάση!");
            } else {
                redirectAttributes.addFlashAttribute("warningMessage",
                        "Η πρακτική άσκηση ολοκληρώθηκε, αλλά με βαθμολογία κάτω από τη βάση.");
            }
            return "redirect:/committee/in-progress";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/committee/finalize?positionId=" + positionId;
        }
    }
}