package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcertSchedulesUseCase;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ConcertApplication.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class GetConcertSchedulesUseCaseIT {
    @Autowired
    private GetConcertSchedulesUseCase getConcertSchedulesUseCase;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private ConcertRepository concertRepository;

    private Concert concert;

    @BeforeEach
    void setUp() {
        concert = Concert.builder()
                .name("Test Concert")
                .info("Test Concert Info")
                .artist("Test Artist")
                .host("Test Host")
                .build();
        concertRepository.save(concert);

        ConcertSchedule schedule1 = ConcertSchedule.builder()
                .concertId(concert.getId())
                .isSoldOut(false)  // 판매 중
                .totalSeat(100)
                .availableSeat(50)
                .dateConcert(LocalDateTime.now().plusDays(1))
                .build();
        concertScheduleRepository.save(schedule1);

        ConcertSchedule schedule2 = ConcertSchedule.builder()
                .concertId(concert.getId())
                .isSoldOut(true)  // 판매 완료
                .totalSeat(100)
                .availableSeat(0)
                .dateConcert(LocalDateTime.now().plusDays(2))
                .build();
        concertScheduleRepository.save(schedule2);
    }

    @Test
    void testGetConcertSchedulesNotSoldOut() {
        // Given
        LocalDateTime dateConcertFrom = LocalDateTime.now();
        LocalDateTime dateConcertTo = LocalDateTime.now().plusDays(3);
        GetConcertSchedulesUseCase.Input input = new GetConcertSchedulesUseCase.Input(0L, concert.getId(), dateConcertFrom, dateConcertTo);

        // When
        GetConcertSchedulesUseCase.Output output = getConcertSchedulesUseCase.execute(input);

        // Then
        assertNotNull(output);
        assertEquals(concert.getId(), output.id());
        assertEquals(concert.getName(), output.name());
        assertEquals("Test Concert Info", output.info());
        assertEquals("Test Artist", output.artist());
        assertEquals("Test Host", output.host());

        List<GetConcertSchedulesUseCase.Output.Item> concertSchedules = output.schedules();
        assertNotNull(concertSchedules);
        assertEquals(1, concertSchedules.size());
        GetConcertSchedulesUseCase.Output.Item schedule = concertSchedules.get(0);
        assertEquals(false, schedule.isSoldOut());
        assertEquals(100, schedule.totalSeat());
        assertEquals(50, schedule.availableSeat());
    }
}
