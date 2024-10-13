package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.repository.ReservationPhaseRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.LeaveReservationPhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LeaveReservationPhaseUnitTest {

    @Mock
    private ReservationPhaseRepository reservationPhaseRepository;

    @InjectMocks
    private LeaveReservationPhase leaveReservationPhase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Mockito 초기화
    }

    @Test
    void shouldDeleteByUuid() {
        // given
        String uuid = "test-uuid";

        // when
        leaveReservationPhase.execute(uuid);

        // then
        verify(reservationPhaseRepository, times(1)).deleteByUuid(uuid);  // deleteByUuid가 1번 호출되었는지 확인
    }
}
