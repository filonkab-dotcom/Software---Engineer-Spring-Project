package com.Fil.Project.service;

import com.Fil.Project.dto.LogbookEntryRequest;
import com.Fil.Project.dto.StudentRequest;
import com.Fil.Project.model.LogbookEntry;
import com.Fil.Project.model.Student;
import com.Fil.Project.model.TraineeshipApplication;
import com.Fil.Project.model.User;
import com.Fil.Project.repository.LogbookEntryRepository;
import com.Fil.Project.repository.StudentRepository;
import com.Fil.Project.repository.TraineeshipApplicationRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {
    private final StudentRepository studentRepo;
    private final TraineeshipApplicationRepository appRepo;
    private final LogbookEntryRepository logRepo;

    @Autowired
    public StudentService(StudentRepository studentRepo,
                          TraineeshipApplicationRepository appRepo,
                          LogbookEntryRepository logRepo) {
        this.studentRepo = studentRepo;
        this.appRepo = appRepo;
        this.logRepo = logRepo;
    }

    /* ---------- US‑STU‑001: Create / Update Student ---------- */
    @Transactional
    public Student createOrUpdateStudent(@NotNull User baseUser, @NotNull StudentRequest req) {
        Student student = (baseUser instanceof Student s) ? s : new Student();
        student.setId(baseUser.getId());                 // copy PK from User
        student.setFirstName(baseUser.getFirstName());   // keep basic data
        student.setLastName(baseUser.getLastName());
        student.setEmail(baseUser.getEmail());
        student.setPassword(baseUser.getPassword());
        student.setUniversityId(req.universityId());
        student.setInterests(req.interests());
        student.setSkills(req.skills());
        student.setPreferredLocation(req.preferredLocation());
        return studentRepo.save(student);
    }

    /* ---------- US‑STU‑002: Apply for traineeship ---------- */
    @Transactional
    public TraineeshipApplication apply(long studentId, long traineeshipId) {
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalStateException("Student not found"));

        if (appRepo.existsByStudentIdAndStatus(studentId,
                TraineeshipApplication.Status.PENDING)) throw new IllegalStateException("Pending application already exists");
        TraineeshipApplication app = new TraineeshipApplication();
        app.setStudent(student);
        app.setTraineeshipId(traineeshipId);
        return appRepo.save(app);
    }

    /* ---------- US‑STU‑003: Add logbook entry ---------- */
    @Transactional
    public LogbookEntry addLog(long applicationId, LogbookEntryRequest req) {
        TraineeshipApplication app = appRepo.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        if (app.getStatus() != TraineeshipApplication.Status.APPROVED)
            throw new IllegalStateException("Application not approved");
        LogbookEntry entry = new LogbookEntry();
        entry.setApplication(app);
        entry.setEntryDate(req.entryDate());
        entry.setDescription(req.description());
        return logRepo.save(entry);
    }
}