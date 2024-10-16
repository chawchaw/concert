package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcertSchedulesNotSoldOut;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

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
                .build();
        concertRepository.save(concert);

        ConcertSchedule schedule1 = ConcertSchedule.builder()
                .concertId(concert.getId())
                .isSold(false)  // 판매 중
                .build();
        concertScheduleRepository.save(schedule1);

        ConcertSchedule schedule2 = ConcertSchedule.builder()
                .concertId(concert.getId())
                .isSold(true)  // 판매 완료
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
        List<ConcertSchedule> concertSchedules = output.concertSchedules();
        assertNotNull(concertSchedules);
        assertEquals(1, concertSchedules.size());
        assertEquals(false, concertSchedules.get(0).getIsSold());
    }
}
