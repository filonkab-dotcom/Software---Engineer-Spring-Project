package com.Fil.Project.controller;

import com.Fil.Project.dto.LogbookEntryRequest;
import com.Fil.Project.dto.StudentRequest;
import com.Fil.Project.model.LogbookEntry;
import com.Fil.Project.model.Student;
import com.Fil.Project.model.TraineeshipApplication;
import com.Fil.Project.repository.LogbookEntryRepository;
import com.Fil.Project.repository.StudentRepository;
import com.Fil.Project.repository.TraineeshipApplicationRepository;
import com.Fil.Project.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final TraineeshipApplicationRepository applicationRepository;
    private final LogbookEntryRepository logbookEntryRepository;

    @Autowired
    public StudentController(StudentService studentService,
                             StudentRepository studentRepository,
                             TraineeshipApplicationRepository applicationRepository,
                             LogbookEntryRepository logbookEntryRepository) {
        this.studentService = studentService;
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
        this.logbookEntryRepository = logbookEntryRepository;
    }

    /**
     * Επιστρέφει τον τρέχοντα συνδεδεμένο φοιτητή ή null αν δεν είναι φοιτητής.
     */
    private Student getCurrentStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof Student) {
                return (Student) principal;
            }
        }
        return null;
    }

    /**
     * Εμφανίζει τη σελίδα προφίλ του φοιτητή.
     */
    @GetMapping("/profile")
    public String showProfile(Model model) {
        Student student = getCurrentStudent();
        if (student == null) {
            return "redirect:/login";
        }

        // Δημιουργία αντικειμένου StudentRequest για τη φόρμα
        StudentRequest studentForm = new StudentRequest(
                student.getUniversityId(),
                student.getInterests(),
                student.getSkills(),
                student.getPreferredLocation()
        );

        model.addAttribute("studentForm", studentForm);
        model.addAttribute("user", student);

        // Έλεγχος για υπάρχουσες αιτήσεις πρακτικής
        List<TraineeshipApplication> applications = applicationRepository.findByStudentId(student.getId());
        if (!applications.isEmpty()) {
            String applicationStatus = applications.get(0).getStatus().name();
            model.addAttribute("applicationStatus", applicationStatus);
        } else {
            model.addAttribute("applicationStatus", "NONE");
        }

        // Μέτρηση καταχωρίσεων ημερολογίου
        Long logbookEntries = 0L;
        for (TraineeshipApplication app : applications) {
            if (app.getStatus() == TraineeshipApplication.Status.APPROVED) {
                logbookEntries = logbookEntryRepository.countByApplicationId(app.getId());
                break;
            }
        }
        model.addAttribute("logbookEntries", logbookEntries);

        return "student/profile";
    }

    /**
     * Επεξεργάζεται την ενημέρωση του προφίλ φοιτητή.
     */
    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("studentForm") StudentRequest studentRequest,
                                BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("user", getCurrentStudent());
            return "student/profile";
        }

        try {
            Student student = getCurrentStudent();
            if (student == null) {
                return "redirect:/login";
            }

            studentService.createOrUpdateStudent(student, studentRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Το προφίλ σας ενημερώθηκε επιτυχώς!");
            return "redirect:/student/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", getCurrentStudent());
            return "student/profile";
        }
    }

    /**
     * Εμφανίζει τη σελίδα αίτησης για πρακτική άσκηση.
     */
    @GetMapping("/apply")
    public String showApplicationForm(Model model, @RequestParam(required = false) Boolean new_application) {
        Student student = getCurrentStudent();
        if (student == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", student);

        // Έλεγχος για υπάρχουσες αιτήσεις
        List<TraineeshipApplication> applications = applicationRepository.findByStudentId(student.getId());
        boolean existingApplication = !applications.isEmpty() &&
                applications.get(0).getStatus() != TraineeshipApplication.Status.REJECTED;

        // Αν υπάρχει αίτηση και δεν έχει απορριφθεί, δείξε την κατάσταση
        if (existingApplication && new_application == null) {
            TraineeshipApplication application = applications.get(0);
            model.addAttribute("existingApplication", true);
            model.addAttribute("application", application);

            // Αν η αίτηση έχει εγκριθεί, φόρτωσε πληροφορίες για τη θέση
            if (application.getStatus() == TraineeshipApplication.Status.APPROVED) {
                // Εδώ θα προσθέταμε κώδικα για να φορτώσουμε τις πληροφορίες της θέσης
                // που έχει ανατεθεί στον φοιτητή
                Map<String, Object> assignedPosition = new HashMap<>();
                assignedPosition.put("companyName", "Tech Solutions SA");
                assignedPosition.put("description", "Java Developer Internship");
                assignedPosition.put("startDate", LocalDate.of(2025, 6, 1));
                assignedPosition.put("endDate", LocalDate.of(2025, 8, 31));
                assignedPosition.put("assignedDate", LocalDate.of(2025, 5, 1));

                model.addAttribute("assignedPosition", assignedPosition);
            }
        } else {
            model.addAttribute("existingApplication", false);
        }

        return "student/apply";
    }

    /**
     * Επεξεργάζεται την υποβολή αίτησης για πρακτική άσκηση.
     */
    @PostMapping("/apply")
    public String submitApplication(@RequestParam String motivationLetter,
                                    @RequestParam(required = false) String additionalInfo,
                                    @RequestParam(required = false) Boolean termsAgreement,
                                    @RequestParam(required = false) Boolean dataAgreement,
                                    Model model, RedirectAttributes redirectAttributes) {
        Student student = getCurrentStudent();
        if (student == null) {
            return "redirect:/login";
        }

        if (termsAgreement == null || !termsAgreement || dataAgreement == null || !dataAgreement) {
            model.addAttribute("errorMessage", "Πρέπει να αποδεχτείτε τους όρους και την επεξεργασία δεδομένων.");
            model.addAttribute("user", student);
            return "student/apply";
        }

        try {
            // Η μέθοδος apply στο service θα χρειαστεί επέκταση για να δέχεται και το motivationLetter
            TraineeshipApplication application = studentService.apply(student.getId(), 0L); // 0L είναι placeholder

            redirectAttributes.addFlashAttribute("successMessage", "Η αίτησή σας υποβλήθηκε επιτυχώς!");
            return "redirect:/student/apply";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", student);
            return "student/apply";
        }
    }

    /**
     * Εμφανίζει τη σελίδα του ημερολογίου πρακτικής άσκησης.
     */
    @GetMapping("/logbook")
    public String showLogbook(Model model) {
        Student student = getCurrentStudent();
        if (student == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", student);

        // Έλεγχος αν ο φοιτητής έχει ενεργή πρακτική άσκηση
        List<TraineeshipApplication> applications = applicationRepository.findByStudent_IdAndStatus(
                student.getId(), TraineeshipApplication.Status.APPROVED);

        boolean activeTraineeship = !applications.isEmpty();
        model.addAttribute("activeTraineeship", activeTraineeship);

        if (activeTraineeship) {
            TraineeshipApplication application = applications.get(0);

            // Φόρτωση πληροφοριών πρακτικής
            Map<String, Object> traineeship = new HashMap<>();
            traineeship.put("companyName", "Tech Solutions SA");
            traineeship.put("location", "Αθήνα");
            traineeship.put("position", "Java Developer Internship");
            traineeship.put("startDate", LocalDate.of(2025, 6, 1));
            traineeship.put("endDate", LocalDate.of(2025, 8, 31));
            traineeship.put("professorName", "Δρ. Μαρία Αντωνίου");
            traineeship.put("professorEmail", "m.antoniou@example.com");

            // Υπολογισμός προόδου
            LocalDate now = LocalDate.now();
            LocalDate startDate = (LocalDate) traineeship.get("startDate");
            LocalDate endDate = (LocalDate) traineeship.get("endDate");

            long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
            long daysPassed = ChronoUnit.DAYS.between(startDate, now);
            int progressPercentage = (int) Math.min(100, Math.max(0, (daysPassed * 100) / totalDays));

            traineeship.put("progressPercentage", progressPercentage);
            model.addAttribute("traineeship", traineeship);

            // Φόρτωση καταχωρίσεων ημερολογίου
            List<LogbookEntry> entries = logbookEntryRepository.findByApplication_IdOrderByEntryDateDesc(application.getId());

            // Προσθήκη πεδίου για το αν μπορεί να γίνει επεξεργασία (εντός 7 ημερών)
            for (LogbookEntry entry : entries) {
                long daysSinceEntry = ChronoUnit.DAYS.between(entry.getEntryDate(), now);
                entry.setEditable(daysSinceEntry <= 7);
            }

            model.addAttribute("entries", entries);
        } else {
            model.addAttribute("entries", Collections.emptyList());
        }

        return "student/logbook";
    }

    /**
     * Προσθέτει νέα καταχώριση στο ημερολόγιο.
     */
    @PostMapping("/logbook/add")
    public String addLogbookEntry(@RequestParam LocalDate entryDate,
                                  @RequestParam String description,
                                  RedirectAttributes redirectAttributes) {
        Student student = getCurrentStudent();
        if (student == null) {
            return "redirect:/login";
        }

        // Έλεγχος αν ο φοιτητής έχει ενεργή πρακτική άσκηση
        List<TraineeshipApplication> applications = applicationRepository.findByStudent_IdAndStatus(
                student.getId(), TraineeshipApplication.Status.APPROVED);

        if (applications.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Δεν έχετε ενεργή πρακτική άσκηση.");
            return "redirect:/student/logbook";
        }

        try {
            TraineeshipApplication application = applications.get(0);
            LogbookEntryRequest request = new LogbookEntryRequest(entryDate, description);
            studentService.addLog(application.getId(), request);

            redirectAttributes.addFlashAttribute("successMessage", "Η καταχώριση προστέθηκε επιτυχώς!");
            return "redirect:/student/logbook";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/student/logbook";
        }
    }

    /**
     * Επεξεργάζεται υπάρχουσα καταχώριση στο ημερολόγιο.
     */
    @PostMapping("/logbook/edit")
    public String editLogbookEntry(@RequestParam Long entryId,
                                   @RequestParam LocalDate entryDate,
                                   @RequestParam String description,
                                   RedirectAttributes redirectAttributes) {
        Student student = getCurrentStudent();
        if (student == null) {
            return "redirect:/login";
        }

        // Έλεγχος αν η καταχώριση υπάρχει και ανήκει στον φοιτητή
        Optional<LogbookEntry> optionalEntry = logbookEntryRepository.findById(entryId);
        if (optionalEntry.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Η καταχώριση δεν βρέθηκε.");
            return "redirect:/student/logbook";
        }

        LogbookEntry entry = optionalEntry.get();
        TraineeshipApplication application = entry.getApplication();

        if (!application.getStudent().getId().equals(student.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Δεν έχετε δικαίωμα επεξεργασίας αυτής της καταχώρισης.");
            return "redirect:/student/logbook";
        }

        // Έλεγχος αν μπορεί να γίνει επεξεργασία (εντός 7 ημερών)
        LocalDate now = LocalDate.now();
        long daysSinceEntry = ChronoUnit.DAYS.between(entry.getEntryDate(), now);
        if (daysSinceEntry > 7) {
            redirectAttributes.addFlashAttribute("errorMessage", "Δεν μπορείτε να επεξεργαστείτε καταχωρίσεις παλαιότερες των 7 ημερών.");
            return "redirect:/student/logbook";
        }

        try {
            entry.setEntryDate(entryDate);
            entry.setDescription(description);
            logbookEntryRepository.save(entry);

            redirectAttributes.addFlashAttribute("successMessage", "Η καταχώριση ενημερώθηκε επιτυχώς!");
            return "redirect:/student/logbook";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/student/logbook";
        }
    }
}