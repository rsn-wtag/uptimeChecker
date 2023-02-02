package com.example.uptimeChecker.Repositories;

import com.example.uptimeChecker.Entities.EodProcessingDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EodProcessingDayRepository extends JpaRepository<EodProcessingDay, Integer> {


}
