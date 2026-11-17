package com.Fil.Project.repository;

import com.Fil.Project.model.LogbookEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LogbookEntryRepository extends JpaRepository<LogbookEntry, Long> {
    Long countByApplicationId(Long applicationId);
    List<LogbookEntry> findByApplication_IdOrderByEntryDateDesc(Long applicationId);
}