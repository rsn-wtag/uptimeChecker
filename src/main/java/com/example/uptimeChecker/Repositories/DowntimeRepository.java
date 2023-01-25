package com.example.uptimeChecker.Repositories;

import com.example.uptimeChecker.Entities.Downtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
@Repository
public interface DowntimeRepository extends JpaRepository<Downtime, BigInteger> {
    public Downtime findFirstByWebIdOrderByDownTimeIdDesc(Integer webId);
}
