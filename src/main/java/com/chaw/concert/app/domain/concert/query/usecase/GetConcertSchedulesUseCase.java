package com.chaw.concert.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class GetConcertSchedulesUseCase {

    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository concertScheduleRepository;

    public Output execute(Input input) {
        Concert concert = concertRepository.findByIdOrThrow(input.concertId());

        List<ConcertSchedule> concertSchedules = concertScheduleRepository.findByConcertIdAndIsSoldOutAndDateConcertBetween(
                input.concertId(),
                false,
                input.dateConcertFrom(),
                input.dateConcertTo());

        log.info("일정({}) 조회", input.concertId());
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
        Long userId,
        Long concertId,
        LocalDateTime dateConcertFrom,
        LocalDateTime dateConcertTo
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
