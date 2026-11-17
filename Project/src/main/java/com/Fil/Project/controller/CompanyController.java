package com.Fil.Project.controller;

import com.Fil.Project.dto.CompanyEvaluationRequest;
import com.Fil.Project.dto.CompanyProfileRequest;
import com.Fil.Project.dto.PositionAnnouncementRequest;
import com.Fil.Project.model.Company;
import com.Fil.Project.model.CompanyEvaluation;
import com.Fil.Project.model.CompanyPosition;
import com.Fil.Project.repository.CompanyEvaluationRepository;
import com.Fil.Project.repository.CompanyPositionRepository;
import com.Fil.Project.repository.CompanyRepository;
import com.Fil.Project.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller για τη διαχείριση των λειτουργιών της εταιρείας.
 */
@Controller
@RequestMapping("/company")
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyRepository companyRepository;
    private final CompanyPositionRepository positionRepository;
    private final CompanyEvaluationRepository evaluationRepository;

    @Autowired
    public CompanyController(CompanyService companyService,
                             CompanyRepository companyRepository,
                             CompanyPositionRepository positionRepository,
                             CompanyEvaluationRepository evaluationRepository) {
        this.companyService = companyService;
        this.companyRepository = companyRepository;
        this.positionRepository = positionRepository;
        this.evaluationRepository = evaluationRepository;
    }

    /**
     * Επιστρέφει την τρέχουσα συνδεδεμένη εταιρεία ή null αν δεν είναι εταιρεία.
     */
    private Company getCurrentCompany() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof Company) {
                return (Company) principal;
            }
        }
        return null;
    }

    /**
     * Εμφανίζει τη σελίδα προφίλ της εταιρείας.
     */
    @GetMapping("/profile")
    public String showProfile(Model model) {
        Company company = getCurrentCompany();
        if (company == null) {
            return "redirect:/login";
        }

        // Δημιουργία αντικειμένου CompanyProfileRequest για τη φόρμα
        CompanyProfileRequest companyForm = new CompanyProfileRequest(
                company.getCompanyName(),
                company.getLocation(),
                company.getFirstName(),
                company.getLastName(),
                company.getEmail(),
                company.getDescription()
        );

        model.addAttribute("companyForm", companyForm);
        model.addAttribute("user", company);

        // Προσθήκη στατιστικών στοιχείων
        long availablePositions = positionRepository.countByCompanyIdAndStatus(
                company.getId(), CompanyPosition.Status.AVAILABLE);
        long assignedPositions = positionRepository.countByCompanyIdAndStatus(
                company.getId(), CompanyPosition.Status.ASSIGNED);
        long totalPositions = positionRepository.countByCompanyId(company.getId());

        model.addAttribute("availablePositions", availablePositions);
        model.addAttribute("assignedPositions", assignedPositions);
        model.addAttribute("totalPositions", totalPositions);

        return "company/profile";
    }

    /**
     * Επεξεργάζεται την ενημέρωση του προφίλ εταιρείας.
     */
    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("companyForm") CompanyProfileRequest companyRequest,
                                BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("user", getCurrentCompany());
            return "company/profile";
        }

        try {
            Company company = getCurrentCompany();
            if (company == null) {
                return "redirect:/login";
            }

            companyService.createOrUpdateCompany(company, companyRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Το προφίλ της εταιρείας ενημερώθηκε επιτυχώς!");
            return "redirect:/company/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", getCurrentCompany());
            return "company/profile";
        }
    }

    /**
     * Εμφανίζει τη λίστα με τις θέσεις πρακτικής της εταιρείας.
     */
    @GetMapping("/positions")
    public String listPositions(Model model) {
        Company company = getCurrentCompany();
        if (company == null) {
            return "redirect:/login";
        }

        List<CompanyPosition> availablePositions = companyService.listAvailablePositions(company.getId());
        List<CompanyPosition> assignedPositions = companyService.listAssignedPositions(company.getId());

        // Συνδυασμός για προβολή σε modals
        List<CompanyPosition> allPositions = new ArrayList<>();
        allPositions.addAll(availablePositions);
        allPositions.addAll(assignedPositions);

        model.addAttribute("availablePositions", availablePositions);
        model.addAttribute("assignedPositions", assignedPositions);
        model.addAttribute("allPositions", allPositions);
        model.addAttribute("user", company);

        return "company/positions";
    }

    /**
     * Επεξεργάζεται την ανάρτηση νέας θέσης πρακτικής.
     */
    @PostMapping("/positions")
    public String announcePosition(@Valid @ModelAttribute("positionForm") PositionAnnouncementRequest positionRequest,
                                   BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        Company company = getCurrentCompany();
        if (company == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            return listPositions(model);
        }

        try {
            companyService.announcePosition(company.getId(), positionRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Η θέση πρακτικής αναρτήθηκε επιτυχώς!");
            return "redirect:/company/positions";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/company/positions";
        }
    }

    /**
     * Επεξεργάζεται τη διαγραφή μιας θέσης πρακτικής.
     */
    @PostMapping("/positions/{id}/delete")
    public String deletePosition(@PathVariable("id") Long positionId, RedirectAttributes redirectAttributes) {
        Company company = getCurrentCompany();
        if (company == null) {
            return "redirect:/login";
        }

        try {
            companyService.deletePosition(company.getId(), positionId);
            redirectAttributes.addFlashAttribute("successMessage", "Η θέση πρακτικής διαγράφηκε επιτυχώς!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/company/positions";
    }

    /**
     * Εμφανίζει τη φόρμα αξιολόγησης φοιτητή.
     */
    @GetMapping("/evaluate/{id}")
    public String showEvaluationForm(@PathVariable("id") Long positionId, Model model) {
        Company company = getCurrentCompany();
        if (company == null) {
            return "redirect:/login";
        }

        Optional<CompanyPosition> optionalPosition = positionRepository.findById(positionId);
        if (optionalPosition.isEmpty() ||
                !optionalPosition.get().getCompany().getId().equals(company.getId()) ||
                optionalPosition.get().getStatus() != CompanyPosition.Status.ASSIGNED) {
            return "redirect:/company/positions";
        }

        CompanyPosition position = optionalPosition.get();
        model.addAttribute("position", position);
        model.addAttribute("user", company);

        // Έλεγχος αν υπάρχει ήδη αξιολόγηση
        Optional<CompanyEvaluation> existingEvaluation =
                evaluationRepository.findByCompanyIdAndPositionId(company.getId(), positionId);

        if (existingEvaluation.isPresent()) {
            model.addAttribute("evaluationForm", new CompanyEvaluationRequest(
                    existingEvaluation.get().getStudentMotivation(),
                    existingEvaluation.get().getStudentEffectiveness(),
                    existingEvaluation.get().getStudentEfficiency()
            ));
            model.addAttribute("isEdit", true);
        } else {
            model.addAttribute("evaluationForm", new CompanyEvaluationRequest(0, 0, 0));
            model.addAttribute("isEdit", false);
        }

        // Φόρτωση πληροφοριών φοιτητή (θα πρέπει να φορτωθεί από τη βάση)
        // Εδώ χρησιμοποιούμε το assigned_student_id από το position
        model.addAttribute("student", new Object()); // Placeholder

        return "company/evaluate";
    }

    /**
     * Επεξεργάζεται την υποβολή αξιολόγησης φοιτητή.
     */
    @PostMapping("/evaluate")
    public String submitEvaluation(@RequestParam Long positionId,
                                   @Valid @ModelAttribute("evaluationForm") CompanyEvaluationRequest evaluationRequest,
                                   BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        Company company = getCurrentCompany();
        if (company == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            Optional<CompanyPosition> optionalPosition = positionRepository.findById(positionId);
            if (optionalPosition.isPresent()) {
                model.addAttribute("position", optionalPosition.get());
            }
            model.addAttribute("user", company);
            return "company/evaluate";
        }

        try {
            companyService.evaluateStudent(company.getId(), positionId, evaluationRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Η αξιολόγηση υποβλήθηκε επιτυχώς!");
            return "redirect:/company/positions";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/company/evaluate/" + positionId;
        }
    }
}