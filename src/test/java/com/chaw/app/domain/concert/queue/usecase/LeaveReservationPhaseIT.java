package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.queue.entity.ReservationPhase;
import com.chaw.concert.app.domain.concert.queue.repository.ReservationPhaseRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.LeaveReservationPhase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class LeaveReservationPhaseIT {

    @Autowired
    ReservationPhaseRepository reservationPhaseRepository;

    @Autowired LeaveReservationPhase leaveReservationPhase;

    @Test
    @DisplayName("예약 페이즈에서 사용자 제거 테스트")
    public void shouldDeleteReservationPhaseByUuid() {
        // given
        String uuid = "test-uuid";
        Long concertId = 999L;

        // 예약 페이즈에 사용자 추가
        ReservationPhase reservationPhase = ReservationPhase.builder()
                .userId(1L)
                .concertScheduleId(concertId)
                .uuid(uuid)
                .build();
        reservationPhaseRepository.saveAll(List.of(reservationPhase));

        // when
        leaveReservationPhase.execute(new LeaveReservationPhase.Input(uuid));

        // then
        Optional<ReservationPhase> deletedPhase = reservationPhaseRepository.findByUuid(uuid);
        assertTrue(deletedPhase.isEmpty(), "예약 페이즈에서 사용자가 삭제되어야 함");
    }

    @Test
    @DisplayName("존재하지 않는 UUID 삭제 시도")
    void shouldHandleNonExistingUuidGracefully() {
        // given
        String nonExistingUuid = "non-existing-uuid";
        LeaveReservationPhase.Input input = new LeaveReservationPhase.Input(nonExistingUuid);

        // when
        leaveReservationPhase.execute(input);

        // then
        // 에러가 발생하지 않고 실행되어야 함
        assertDoesNotThrow(() -> leaveReservationPhase.execute(input));
    }
}
