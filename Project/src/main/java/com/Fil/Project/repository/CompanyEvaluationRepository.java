package com.Fil.Project.repository;

import com.Fil.Project.model.CompanyEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyEvaluationRepository extends JpaRepository<CompanyEvaluation, Long> {
    List<CompanyEvaluation> findByPositionId(Long positionId);
    Optional<CompanyEvaluation> findByCompanyIdAndPositionId(Long companyId, Long positionId);
}