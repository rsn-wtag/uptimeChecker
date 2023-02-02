package com.example.uptimeChecker.Repositories;

import com.example.uptimeChecker.Entities.DowntimeSummary;
import com.example.uptimeChecker.Entities.DowntimeSummary_PK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DowntimeSummaryRepository extends JpaRepository<DowntimeSummary, DowntimeSummary_PK> {
    List<DowntimeSummary> findDowntimeSummariesByDowntimeSummaryPkWebIdOrderByDowntimeSummaryPkDateDesc(Integer webId);
    boolean existsByDowntimeSummaryPk(DowntimeSummary_PK downtimeSummaryPk);
}
