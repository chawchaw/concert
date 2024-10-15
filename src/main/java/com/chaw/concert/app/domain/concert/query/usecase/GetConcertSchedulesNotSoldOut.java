package com.chaw.concert.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetConcertSchedulesNotSoldOut {

    private final ConcertScheduleRepository concertScheduleRepository;

    public GetConcertSchedulesNotSoldOut(ConcertScheduleRepository concertScheduleRepository) {
        this.concertScheduleRepository = concertScheduleRepository;
    }

    public Output execute(Input input) {
        List<ConcertSchedule> concertSchedules = concertScheduleRepository.findByConcertIdAndIsSold(input.getConcertId(), false);

        return new Output(concertSchedules);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Input {
        private Long concertId;
    }

    @Getter
    @AllArgsConstructor
    public class Output {
        private List<ConcertSchedule> concertSchedules;
    }
}
