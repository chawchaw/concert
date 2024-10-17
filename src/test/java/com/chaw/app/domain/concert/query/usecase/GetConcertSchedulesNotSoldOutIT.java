package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcertSchedulesNotSoldOut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class GetConcertSchedulesNotSoldOutIT {
    @Autowired
    private GetConcertSchedulesNotSoldOut getConcertSchedulesNotSoldOut;

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
                .isSold(false)  // 판매 중
                .totalSeat(100)
                .availableSeat(50)
                .dateConcert(LocalDateTime.now().plusDays(1))
                .build();
        concertScheduleRepository.save(schedule1);

        ConcertSchedule schedule2 = ConcertSchedule.builder()
                .concertId(concert.getId())
                .isSold(true)  // 판매 완료
                .totalSeat(100)
                .availableSeat(0)
                .dateConcert(LocalDateTime.now().plusDays(2))
                .build();
        concertScheduleRepository.save(schedule2);
    }

    @Test
    void testGetConcertSchedulesNotSoldOut() {
        // Given
        GetConcertSchedulesNotSoldOut.Input input = new GetConcertSchedulesNotSoldOut.Input(concert.getId());

        // When
        GetConcertSchedulesNotSoldOut.Output output = getConcertSchedulesNotSoldOut.execute(input);

        // Then
        assertNotNull(output);
        assertEquals(concert.getId(), output.id());
        assertEquals(concert.getName(), output.name());
        assertEquals("Test Concert Info", output.info());
        assertEquals("Test Artist", output.artist());
        assertEquals("Test Host", output.host());

        List<GetConcertSchedulesNotSoldOut.Output.Item> concertSchedules = output.schedules();
        assertNotNull(concertSchedules);
        assertEquals(1, concertSchedules.size());
        GetConcertSchedulesNotSoldOut.Output.Item schedule = concertSchedules.get(0);
        assertEquals(false, schedule.isSold());
        assertEquals(100, schedule.totalSeat());
        assertEquals(50, schedule.availableSeat());
    }
}
