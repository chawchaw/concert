package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.QueuePositionTracker;
import com.chaw.concert.app.domain.concert.queue.repository.QueuePositionTrackerRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.EnterWaitQueue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class EnterWaitQueueIT {

    @Autowired
    private EnterWaitQueue enterWaitQueue;

    @Autowired
    private WaitQueueRepository waitQueueRepository;

    @Autowired
    private QueuePositionTrackerRepository queuePositionTrackerRepository;

    @Test
    @DisplayName("사용자가 대기열에 성공적으로 참가할 수 있어야 한다")
    void shouldEnterQueueSuccessfully() {
        // given
        EnterWaitQueue.Input input = EnterWaitQueue.Input.builder()
                .userId(1L)
                .concertId(1L)
                .build();

        // when
        WaitQueue result = enterWaitQueue.execute(input);

        // then
        assertNotNull(result);
        assertEquals(input.getUserId(), result.getUserId());
        assertEquals(input.getConcertId(), result.getConcertId());
        assertNotNull(result.getUuid());

        // 데이터베이스에 대기열이 추가되었는지 확인
        assertTrue(waitQueueRepository.existsByConcertIdAndUuid(input.getConcertId(), result.getUuid()));

        // 대기열 인디케이터가 생성되었는지 확인
        QueuePositionTracker indicator = queuePositionTrackerRepository.findByConcertId(input.getConcertId());
        assertNotNull(indicator);
        assertTrue(indicator.getIsWaitQueueExist());
    }

    @Test
    @DisplayName("대기열 인디케이터가 있을 때 대기 상태가 아닌 경우 업데이트한다")
    void shouldUpdateIndicatorWhenNotWaiting() {
        // given
        EnterWaitQueue.Input input = EnterWaitQueue.Input.builder()
                .userId(1L)
                .concertId(1L)
                .build();

        // 이미 대기열 인디케이터가 존재하고 대기 상태가 아닌 경우
        QueuePositionTracker indicator = QueuePositionTracker.builder()
                .concertId(input.getConcertId())
                .waitingUserId(0L)
                .isWaitQueueExist(false)
                .build();
        queuePositionTrackerRepository.save(indicator);

        // when
        WaitQueue result = enterWaitQueue.execute(input);

        // then
        assertNotNull(result);
        assertEquals(input.getUserId(), result.getUserId());
        assertEquals(input.getConcertId(), result.getConcertId());
        assertNotNull(result.getUuid());

        // 대기열 인디케이터가 업데이트되었는지 확인
        QueuePositionTracker updatedIndicator = queuePositionTrackerRepository.findByConcertId(input.getConcertId());
        assertTrue(updatedIndicator.getIsWaitQueueExist());
    }
}
