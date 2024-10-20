package com.chaw.concert.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.exception.ConcertNotFoundException;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        Concert concert = concertRepository.findById(input.concertId());
        if (concert == null) {
            throw new ConcertNotFoundException();
        }

        List<ConcertSchedule> concertSchedules = concertScheduleRepository.findByConcertIdAndIsSoldOut(input.concertId(), false);

        return new Output(
                concert.getId(),
                concert.getName(),
                concert.getInfo(),
                concert.getArtist(),
                concert.getHost(),
                concertSchedules.stream().map(schedule -> new Output.Item(
                        schedule.getId(),
                        schedule.getIsSoldOut(),
                        schedule.getTotalSeat(),
                        schedule.getAvailableSeat(),
                        schedule.getDateConcert()
                )).toList()
        );
    }

    public record Input (
        Long concertId
    ) {}

    public record Output (
        Long id,
        String name,
        String info,
        String artist,
        String host,
        List<Item> schedules
    ) {
        public record Item (
                Long id,
                Boolean isSoldOut,
                Integer totalSeat,
                Integer availableSeat,
                LocalDateTime dateConcert
        ) {}
    }
}
