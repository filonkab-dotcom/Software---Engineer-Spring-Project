package com.Fil.Project.controller;

import com.Fil.Project.dto.UserRegistrationRequest;
import com.Fil.Project.model.Company;
import com.Fil.Project.model.Professor;
import com.Fil.Project.model.Student;
import com.Fil.Project.model.User;
import com.Fil.Project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            User user = (User) auth.getPrincipal();
            model.addAttribute("user", user);
        }
        return "index";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Έλεγχος αν ο χρήστης είναι ήδη συνδεδεμένος
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        model.addAttribute("userForm", new UserRegistrationRequest("", "", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userForm") UserRegistrationRequest userForm,
                               BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.register(userForm);
            redirectAttributes.addFlashAttribute("successMessage", "Η εγγραφή ολοκληρώθηκε επιτυχώς!");
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        // Έλεγχος αν ο χρήστης είναι ήδη συνδεδεμένος
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/logout-confirm")
    public String showLogoutConfirmation(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            User user = (User) auth.getPrincipal();
            model.addAttribute("user", user);
            return "logout-confirmation";
        }
        return "redirect:/login";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            userService.logout(request, response);
        }
        return "redirect:/login?logout";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            User user = (User) auth.getPrincipal();

            // Ανάλογα με τον τύπο χρήστη, ανακατεύθυνση στο κατάλληλο dashboard
            if (user instanceof Student) {
                return "redirect:/student/profile";
            } else if (user instanceof Professor) {
                return "redirect:/professor/profile";
            } else if (user instanceof Company) {
                return "redirect:/company/profile";
            } else {
                // Υποθέτουμε ότι είναι μέλος της επιτροπής
                return "redirect:/committee/applications";
            }
        }
        return "redirect:/login";
    }
}