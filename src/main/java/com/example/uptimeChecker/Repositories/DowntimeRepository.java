package com.example.uptimeChecker.Repositories;

import com.example.uptimeChecker.Entities.Downtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Set;

@Repository
public interface DowntimeRepository extends JpaRepository<Downtime, BigInteger> {
    public Downtime findFirstByWebIdOrderByDownTimeIdDesc(Integer webId);

    public Set<Downtime> findByDateAndWebId(Date date, Integer webId);

    Set<Downtime> findByEndTime(OffsetDateTime endTime);

    Set<Downtime> findByDateAndEndTime(Date date, OffsetDateTime endTime);

    @Query("select d from Downtime d where d.date=:date and (d.endTime=null or d.startTime>d.endTime)")
    Set<Downtime> findByDateAndEndTimeGreaterThanStartTime( Date date);

    void deleteByWebIdAndDate(Integer webId, Date date);
}
