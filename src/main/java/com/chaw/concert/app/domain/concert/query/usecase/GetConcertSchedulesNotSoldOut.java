package com.chaw.concert.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.exception.ConcertNotFound;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetConcertSchedulesNotSoldOut {

    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository concertScheduleRepository;

    public GetConcertSchedulesNotSoldOut(ConcertRepository concertRepository, ConcertScheduleRepository concertScheduleRepository) {
        this.concertRepository = concertRepository;
        this.concertScheduleRepository = concertScheduleRepository;
    }

    public Output execute(Input input) {
        Boolean exists = concertRepository.existsById(input.concertId());
        if (!exists) {
            throw new ConcertNotFound();
        }

        List<ConcertSchedule> concertSchedules = concertScheduleRepository.findByConcertIdAndIsSold(input.concertId(), false);

        return new Output(concertSchedules);
    }

    public record Input (
        Long concertId
    ) {}

    public record Output (
        List<ConcertSchedule> concertSchedules
    ) {}
}
