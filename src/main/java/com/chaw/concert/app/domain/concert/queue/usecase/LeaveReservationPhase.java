package com.chaw.concert.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.repository.ReservationPhaseRepository;
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

    public void execute(String uuid) {
        reservationPhaseRepository.deleteByUuid(uuid);
    }
}
