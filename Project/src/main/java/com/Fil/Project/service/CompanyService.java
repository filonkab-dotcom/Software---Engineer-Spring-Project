package com.Fil.Project.service;

import com.Fil.Project.dto.CompanyEvaluationRequest;
import com.Fil.Project.dto.CompanyProfileRequest;
import com.Fil.Project.dto.PositionAnnouncementRequest;
import com.Fil.Project.model.Company;
import com.Fil.Project.model.CompanyEvaluation;
import com.Fil.Project.model.CompanyPosition;
import com.Fil.Project.model.User;
import com.Fil.Project.repository.CompanyEvaluationRepository;
import com.Fil.Project.repository.CompanyPositionRepository;
import com.Fil.Project.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CompanyService {
    private final CompanyRepository companyRepo;
    private final CompanyPositionRepository posRepo;
    private final CompanyEvaluationRepository evalRepo;

    @Autowired
    public CompanyService(CompanyRepository c, CompanyPositionRepository p, CompanyEvaluationRepository e) {
        this.companyRepo = c;
        this.posRepo = p;
        this.evalRepo = e;
    }

    @Transactional
    public Company createOrUpdateCompany(User base, CompanyProfileRequest req) {
        Company comp = (base instanceof Company co) ? co : new Company();
        comp.setId(base.getId());
        comp.setFirstName(req.firstName());
        comp.setLastName(req.lastName());
        comp.setEmail(base.getEmail());
        comp.setPassword(base.getPassword());
        comp.setCompanyName(req.companyName());
        comp.setLocation(req.location());
        comp.setDescription(req.description());
        return companyRepo.save(comp);
    }

    public List<CompanyPosition> listAvailablePositions(Long id) {
        return posRepo.findByCompanyIdAndStatus(id, CompanyPosition.Status.AVAILABLE);
    }

    public List<CompanyPosition> listAssignedPositions(Long id) {
        return posRepo.findByCompanyIdAndStatus(id, CompanyPosition.Status.ASSIGNED);
    }

    @Transactional
    public CompanyPosition announcePosition(Long id, PositionAnnouncementRequest req) {
        Company comp = companyRepo.findById(id).orElseThrow();
        CompanyPosition pos = new CompanyPosition();
        pos.setCompany(comp);
        pos.setStartDate(req.startDate());
        pos.setEndDate(req.endDate());
        pos.setDescription(req.description());
        pos.setRequiredSkills(req.requiredSkills());
        pos.setTopics(req.topics());
        return posRepo.save(pos);
    }

    @Transactional
    public void deletePosition(Long compId, Long posId) {
        CompanyPosition pos = posRepo.findById(posId).orElseThrow();
        if(!pos.getCompany().getId().equals(compId))
            throw new IllegalStateException("Position not owned");
        pos.setStatus(CompanyPosition.Status.DELETED);
        posRepo.save(pos);
    }

    @Transactional
    public CompanyEvaluation evaluateStudent(Long compId, Long posId, CompanyEvaluationRequest req) {
        Company comp = companyRepo.findById(compId).orElseThrow();
        CompanyPosition pos = posRepo.findById(posId).orElseThrow();
        if(!pos.getCompany().getId().equals(compId) || pos.getStatus() != CompanyPosition.Status.ASSIGNED)
            throw new IllegalStateException("Cannot eval");
        CompanyEvaluation ev = new CompanyEvaluation();
        ev.setCompany(comp);
        ev.setPositionId(posId);
        ev.setStudentMotivation(req.studentMotivation());
        ev.setStudentEffectiveness(req.studentEffectiveness());
        ev.setStudentEfficiency(req.studentEfficiency());
        return evalRepo.save(ev);
    }
}