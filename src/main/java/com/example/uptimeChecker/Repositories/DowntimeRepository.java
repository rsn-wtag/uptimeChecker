package com.example.uptimeChecker.Repositories;

import com.example.uptimeChecker.Entities.Downtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface DowntimeRepository extends JpaRepository<Downtime, BigInteger> {
    Downtime findFirstByWebIdOrderByDownTimeIdDesc(Integer webId);

    List<Downtime> findByDateAndWebId(Date date, Integer webId);

    List<Downtime> findByEndTime(OffsetTime endTime);

    Set<Downtime> findByDateAndEndTime(Date date, OffsetTime endTime);

    @Query("select d from Downtime d where d.date=:date and (d.endTime=null or d.startTime>d.endTime)")
    List<Downtime> findByDateAndEndTimeGreaterThanStartTime( Date date);

    //@Lock(LockModeType.PESSIMISTIC_WRITE)
    void deleteByWebIdAndDate(Integer webId, Date date);

    Downtime findFirstByOrderByDateDesc();

    @Modifying
    @Query("update Downtime set endTime=:endTime where endTime is null")
    void updateAllNullEndTime(OffsetTime endTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Downtime> findById(BigInteger id);

    List<Downtime> findByDate(Date date);
}
