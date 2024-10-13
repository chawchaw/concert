package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.queue.entity.QueuePositionTracker;
import com.chaw.concert.app.domain.concert.queue.entity.ReservationPhase;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.exception.UserNotInQueueException;
import com.chaw.concert.app.domain.concert.queue.repository.QueuePositionTrackerRepository;
import com.chaw.concert.app.domain.concert.queue.repository.ReservationPhaseRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.GetWaitQueueStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
class GetWaitQueueStatusIT {

    @Autowired
    private GetWaitQueueStatus getWaitQueueStatus;

    @Autowired
    private QueuePositionTrackerRepository queuePositionTrackerRepository;

    @Autowired
    private WaitQueueRepository waitQueueRepository;

    @Autowired
    private ReservationPhaseRepository reservationPhaseRepository;

    @BeforeEach
    void setUp() {
        // 필요한 데이터 설정 (대기열, 예약 페이즈 데이터 등)
    }

    @Test
    @DisplayName("예약페이즈에 있으면 대기열 순번이 0으로 리턴된다.")
    void shouldReturnZeroWhenUserIsInReservationPhase() {
        // given
        Long concertId = 1L;
        String uuid = "test-uuid";

        ReservationPhase reservationPhase = ReservationPhase.builder()
                .concertId(concertId)
                .uuid(uuid)
                .build();
        reservationPhaseRepository.save(reservationPhase);

        GetWaitQueueStatus.Input input = GetWaitQueueStatus.Input.builder()
                .concertId(concertId)
                .uuid(uuid)
                .build();

        // when
        GetWaitQueueStatus.Output result = getWaitQueueStatus.execute(input);

        // then
        assertNotNull(result);
        assertEquals(0, result.getQueuePosition());
        assertTrue(result.getIsReservationPhase());
    }

    @Test
    @DisplayName("대기열에 사용자가 존재하지 않을 때 예외가 발생한다.")
    void shouldThrowUserNotInQueueException() {
        // given
        Long concertId = 1L;
        String uuid = "non-existent-uuid";

        QueuePositionTracker tracker = QueuePositionTracker.builder()
                .concertId(concertId)
                .waitingUserId(10L)
                .build();
        queuePositionTrackerRepository.save(tracker);

        GetWaitQueueStatus.Input input = GetWaitQueueStatus.Input.builder()
                .concertId(concertId)
                .uuid(uuid)
                .build();

        // when & then
        assertThrows(UserNotInQueueException.class, () -> {
            getWaitQueueStatus.execute(input);
        });
    }

    @Test
    @DisplayName("대기열에 사용자가 있으면 순번을 정상적으로 리턴한다.")
    void shouldReturnCorrectQueuePosition() {
        // given
        Long concertId = 1L;
        String uuid = "test-uuid";

        QueuePositionTracker tracker = QueuePositionTracker.builder()
                .concertId(concertId)
                .waitingUserId(10L)
                .build();
        queuePositionTrackerRepository.save(tracker);

        WaitQueue waitQueue = WaitQueue.builder()
                .concertId(concertId)
                .userId(1L)
                .uuid(uuid)
                .build();
        waitQueueRepository.save(waitQueue);

        GetWaitQueueStatus.Input input = GetWaitQueueStatus.Input.builder()
                .concertId(concertId)
                .uuid(uuid)
                .build();

        // when
        GetWaitQueueStatus.Output result = getWaitQueueStatus.execute(input);

        // then
        assertNotNull(result);
        assertEquals(1, result.getQueuePosition());  // 대기열 순번이 1이어야 함
        assertFalse(result.getIsReservationPhase());
    }

    @Test
    @DisplayName("대기열 시작후 첫 3명이 대기할 때 2번째 사람의 대기 순서는 2번이다")
    void shouldReturnCorrectQueuePositionWithMultipleWaitQueue() {
        // given
        Long concertId = 999L;
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long userId3 = 3L;
        String uuid1 = "test-uuid1";
        String uuid2 = "test-uuid2";
        String uuid3 = "test-uuid3";

        // 프로세스 시작
        QueuePositionTracker queuePositionTracker = QueuePositionTracker.builder()
                .concertId(concertId)
                .waitingUserId(0L)  // 아무 사용자도 입장하지 않음
                .build();
        queuePositionTrackerRepository.save(queuePositionTracker);

        // 대기열에 사용자 추가
        waitQueueRepository.save(WaitQueue.builder().concertId(concertId).userId(userId1).uuid(uuid1).build());
        waitQueueRepository.save(WaitQueue.builder().concertId(concertId).userId(userId2).uuid(uuid2).build());
        waitQueueRepository.save(WaitQueue.builder().concertId(concertId).userId(userId3).uuid(uuid3).build());

        // 입력 생성
        GetWaitQueueStatus.Input input = GetWaitQueueStatus.Input.builder()
                .concertId(concertId)
                .uuid(uuid2)
                .build();

        // when
        GetWaitQueueStatus.Output result = getWaitQueueStatus.execute(input);

        // then
        assertNotNull(result);
        assertEquals(2, result.getQueuePosition());  // 2번째 사용자는 2번
    }

    @Test
    @DisplayName("대기열에 총 10명이 있고 5명 입장후에 9번째 사람의 대기 순서는 4번이다")
    void shouldReturnCorrectQueuePosition4th() {
        // given
        Long concertId = 999L;
        List<WaitQueue> waitQueues = List.of(
                WaitQueue.builder().concertId(concertId).userId(1L).uuid("test-uuid1").build(),
                WaitQueue.builder().concertId(concertId).userId(2L).uuid("test-uuid2").build(),
                WaitQueue.builder().concertId(concertId).userId(3L).uuid("test-uuid3").build(),
                WaitQueue.builder().concertId(concertId).userId(4L).uuid("test-uuid4").build(),
                WaitQueue.builder().concertId(concertId).userId(5L).uuid("test-uuid5").build(),
                WaitQueue.builder().concertId(concertId).userId(6L).uuid("test-uuid6").build(),
                WaitQueue.builder().concertId(concertId).userId(7L).uuid("test-uuid7").build(),
                WaitQueue.builder().concertId(concertId).userId(8L).uuid("test-uuid8").build(),
                WaitQueue.builder().concertId(concertId).userId(9L).uuid("test-uuid9").build(),
                WaitQueue.builder().concertId(concertId).userId(10L).uuid("test-uuid10").build()
        );
        WaitQueue waitQueue5Th = waitQueues.get(4);
        WaitQueue waitQueue9Th = waitQueues.get(8);

        // 대기열에 사용자 추가
        waitQueues.forEach(waitQueueRepository::save);

        // 콘서트 대기열 시작
        QueuePositionTracker queuePositionTracker = QueuePositionTracker.builder()
                .concertId(concertId)
                .waitingUserId(waitQueue5Th.getId())  // 5번째 사용자까지 입장함
                .build();
        queuePositionTrackerRepository.save(queuePositionTracker);

        // 입력 생성
        GetWaitQueueStatus.Input input = GetWaitQueueStatus.Input.builder()
                .concertId(concertId)
                .uuid(waitQueue9Th.getUuid())
                .build();

        // when
        GetWaitQueueStatus.Output result = getWaitQueueStatus.execute(input);

        // then
        assertNotNull(result);
        assertEquals(4, result.getQueuePosition());  // 2번째 사용자는 대기열에서 2번
    }

    @Test
    @DisplayName("2개의 콘서트는 각자 대기열을 갖는다" +
            "콘서트1은 3명이 대기하고 아무도 입장하지 않았으므로 1번째 사용자의 대기순서는 1번 " +
            "콘서트2는 3명이 대기하고 1번째 사용자가 입장했으므로 3번째 사용자의 대기순서는 2번" +
            "각 콘서트의 대기열에는 한번씩 엇갈리면서 입장한다")
    void shouldReturnCorrectQueuePositionWithMultipleConcerts() {
        // given
        Long concertId1 = 998L;
        Long concertId2 = 999L;
        List<WaitQueue> waitingUsers1 = List.of(
                WaitQueue.builder().concertId(concertId1).userId(1L).uuid("test-uuid1").build(),
                WaitQueue.builder().concertId(concertId1).userId(2L).uuid("test-uuid2").build(),
                WaitQueue.builder().concertId(concertId1).userId(3L).uuid("test-uuid3").build()
        );
        List<WaitQueue> waitingUsers2 = List.of(
                WaitQueue.builder().concertId(concertId2).userId(4L).uuid("test-uuid4").build(),
                WaitQueue.builder().concertId(concertId2).userId(5L).uuid("test-uuid5").build(),
                WaitQueue.builder().concertId(concertId2).userId(6L).uuid("test-uuid6").build()
        );

        // 대기열에 사용자 추가, 서로 엇갈려서 입장
        int maxSize = Math.max(waitingUsers1.size(), waitingUsers2.size());
        for (int i = 0; i < maxSize; i++) {
            if (i < waitingUsers1.size()) {
                waitQueueRepository.save(waitingUsers1.get(i));
            }
            if (i < waitingUsers2.size()) {
                waitQueueRepository.save(waitingUsers2.get(i));
            }
        }

        // 콘서트 대기열 시작
        QueuePositionTracker queuePositionTracker1 = QueuePositionTracker.builder()
                .concertId(concertId1)
                .waitingUserId(0L)  // 콘서트1은 아무 사용자도 입장하지 않음
                .build();
        queuePositionTrackerRepository.save(queuePositionTracker1);
        QueuePositionTracker queuePositionTracker2 = QueuePositionTracker.builder()
                .concertId(concertId2)
                .waitingUserId(waitingUsers2.get(0).getId())  // 콘서트2는 1번째 사용자까지 입장함
                .build();
        queuePositionTrackerRepository.save(queuePositionTracker2);

        // 입력 생성
        GetWaitQueueStatus.Input input1 = GetWaitQueueStatus.Input.builder()
                .concertId(concertId1)
                .uuid(waitingUsers1.get(0).getUuid()) // 콘서트1의 1번째 사용자의 대기열 순서
                .build();
        GetWaitQueueStatus.Input input2 = GetWaitQueueStatus.Input.builder()
                .concertId(concertId2)
                .uuid(waitingUsers2.get(2).getUuid()) // 콘서트2의 3번째 사용자의 대기열 순서
                .build();

        // when
        GetWaitQueueStatus.Output result1 = getWaitQueueStatus.execute(input1);
        GetWaitQueueStatus.Output result2 = getWaitQueueStatus.execute(input2);

        // then
        assertNotNull(result1);
        assertEquals(1, result1.getQueuePosition());  // 콘서트1의 2번째 사용자 대기열 순서
        assertEquals(2, result2.getQueuePosition());  // 콘서트2의 2번째 사용자 대기열 순서
    }
}
