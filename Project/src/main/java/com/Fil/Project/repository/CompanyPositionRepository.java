package com.Fil.Project.repository;

import com.Fil.Project.model.CompanyPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CompanyPositionRepository extends JpaRepository<CompanyPosition, Long> {
    List<CompanyPosition> findByCompanyIdAndStatus(Long companyId, CompanyPosition.Status status);
    List<CompanyPosition> findByStatus(CompanyPosition.Status status);
    long countByCompanyIdAndStatus(Long companyId, CompanyPosition.Status status);
    long countByCompanyId(Long companyId);
}
