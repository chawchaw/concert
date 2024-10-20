package com.chaw.concert.app.infrastructure.mysql.conert.query;

import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConcertScheduleJpaRepository extends JpaRepository<ConcertSchedule, Long> {
    List<ConcertSchedule> findByConcertIdAndIsSoldOut(Long concertId, boolean isSoldOut);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cs FROM ConcertSchedule cs WHERE cs.id = :id")
    ConcertSchedule findByIdWithLock(Long id);

    @Modifying
    @Query("" +
            "UPDATE ConcertSchedule cs " +
            "SET cs.availableSeat = cs.availableSeat - 1, " +
            "    cs.isSoldOut = CASE WHEN cs.availableSeat = 0 THEN true ELSE false END " +
            "WHERE cs.id = :concertScheduleId AND cs.availableSeat > 0")
    int decreaseAvailableSeat(Long concertScheduleId);
}
