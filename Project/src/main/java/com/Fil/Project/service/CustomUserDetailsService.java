package com.Fil.Project.service;

import com.Fil.Project.model.Company;
import com.Fil.Project.model.Professor;
import com.Fil.Project.model.Student;
import com.Fil.Project.model.User;
import com.Fil.Project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        User user = userOpt.get();

        // Έλεγχος για κλειδωμένο λογαριασμό
        if (!user.isAccountNonLocked()) {
            // Έλεγχος αν έχει λήξει το κλείδωμα (15 λεπτά)
            LocalDateTime lockTime = user.getLockTime();
            if (lockTime != null && lockTime.plusMinutes(15).isBefore(LocalDateTime.now())) {
                // Ξεκλείδωμα λογαριασμού
                user.setAccountNonLocked(true);
                user.setLockTime(null);
                user.setFailedAttempts(0);
                userRepository.save(user);
            }
        }

        // Προσδιορισμός ρόλου με βάση τον τύπο χρήστη
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (user instanceof Student) {
            authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
        } else if (user instanceof Professor) {
            authorities.add(new SimpleGrantedAuthority("ROLE_PROFESSOR"));
        } else if (user instanceof Company) {
            authorities.add(new SimpleGrantedAuthority("ROLE_COMPANY"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_COMMITTEE"));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isAccountNonLocked(),
                true, // account not expired
                true, // credentials not expired
                true, // account enabled
                authorities
        );
    }
}