package com.chaw.concert.app.infrastructure.mysql.conert.query;

import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Repository
public class ConcertScheduleRepositoryImpl implements ConcertScheduleRepository {

    private final ConcertScheduleJpaRepository repository;

    @Override
    public List<ConcertSchedule> findByConcertIdAndIsSoldOutAndDateConcertBetween(Long concertId, boolean isSoldOut, LocalDateTime dateConcertFrom, LocalDateTime dateConcertTo) {
        return repository.findByConcertIdAndIsSoldOutAndDateConcertBetween(concertId, isSoldOut, dateConcertFrom, dateConcertTo);
    }

    @Override
    public ConcertSchedule findByIdOrThrow(Long id) {
        ConcertSchedule concertSchedule = repository.findById(id).orElse(null);
        throwNotFoundException(concertSchedule);
        return concertSchedule;
    }

    @Override
    public ConcertSchedule save(ConcertSchedule concertSchedule) {
        return repository.save(concertSchedule);
    }

    @Override
    public boolean decreaseAvailableSeat(Long concertScheduleId) {
        return repository.decreaseAvailableSeat(concertScheduleId) > 0;
    }

    private void throwNotFoundException(ConcertSchedule concertSchedule) {
        if (concertSchedule == null) {
            throw new BaseException(ErrorType.NOT_FOUND, "없는 일정입니다.");
        }
    }
}
