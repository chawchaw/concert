package com.chaw.concert.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.repository.ReservationPhaseRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

/**
 * 예약페이즈를 종료한다
 */
@Service
public class LeaveReservationPhase {

    private final ReservationPhaseRepository reservationPhaseRepository;

    public LeaveReservationPhase(ReservationPhaseRepository reservationPhaseRepository) {
        this.reservationPhaseRepository = reservationPhaseRepository;
    }

    public Output execute(Input input) {
        reservationPhaseRepository.deleteByUuid(input.getUuid());
        return new Output(true);
    }

    @Getter
    @AllArgsConstructor
    public static class Input {
        private String uuid;
    }

    @Getter
    @AllArgsConstructor
    public class Output {
        private Boolean result;
    }
}
