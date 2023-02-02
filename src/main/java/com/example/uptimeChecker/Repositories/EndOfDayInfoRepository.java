package com.example.uptimeChecker.Repositories;

import com.example.uptimeChecker.Entities.EndOfDayInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface EndOfDayInfoRepository extends JpaRepository<EndOfDayInfo, BigInteger> {
}
