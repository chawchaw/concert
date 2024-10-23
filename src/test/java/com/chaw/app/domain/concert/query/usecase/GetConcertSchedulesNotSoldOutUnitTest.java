package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcertSchedulesNotSoldOut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class GetConcertSchedulesNotSoldOutUnitTest {

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private ConcertScheduleRepository concertScheduleRepository;

    @InjectMocks
    private GetConcertSchedulesNotSoldOut getConcertSchedulesNotSoldOut;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecute_ConcertFound() {
        // Given
        Long concertId = 1L;
        Concert concert = Concert.builder()
                .id(concertId)
                .name("Test Concert")
                .info("Test Concert Info")
                .artist("Test Artist")
                .host("Test Host")
                .build();

        when(concertRepository.findById(concertId)).thenReturn(concert);

        List<ConcertSchedule> concertSchedules = Arrays.asList(
                ConcertSchedule.builder()
                        .id(1L)
                        .concertId(concertId)
                        .isSoldOut(false)
                        .totalSeat(100)
                        .availableSeat(50)
                        .dateConcert(LocalDateTime.now().plusDays(1))
                        .build(),
                ConcertSchedule.builder()
                        .id(2L)
                        .concertId(concertId)
                        .isSoldOut(false)
                        .totalSeat(200)
                        .availableSeat(100)
                        .dateConcert(LocalDateTime.now().plusDays(2))
                        .build()
        );

        when(concertScheduleRepository.findByConcertIdAndIsSoldOut(concertId, false)).thenReturn(concertSchedules);

        // When
        GetConcertSchedulesNotSoldOut.Input input = new GetConcertSchedulesNotSoldOut.Input(concertId);
        GetConcertSchedulesNotSoldOut.Output output = getConcertSchedulesNotSoldOut.execute(input);

        // Then
        assertNotNull(output);
        assertEquals(concertId, output.id());
        assertEquals("Test Concert", output.name());
        assertEquals("Test Concert Info", output.info());
        assertEquals("Test Artist", output.artist());
        assertEquals("Test Host", output.host());

        List<GetConcertSchedulesNotSoldOut.Output.Item> scheduleItems = output.schedules();
        assertEquals(2, scheduleItems.size());

        GetConcertSchedulesNotSoldOut.Output.Item firstSchedule = scheduleItems.get(0);
        assertEquals(1L, firstSchedule.id());
        assertEquals(false, firstSchedule.isSoldOut());
        assertEquals(100, firstSchedule.totalSeat());
        assertEquals(50, firstSchedule.availableSeat());

        GetConcertSchedulesNotSoldOut.Output.Item secondSchedule = scheduleItems.get(1);
        assertEquals(2L, secondSchedule.id());
        assertEquals(false, secondSchedule.isSoldOut());
        assertEquals(200, secondSchedule.totalSeat());
        assertEquals(100, secondSchedule.availableSeat());

        verify(concertRepository, times(1)).findById(concertId);
        verify(concertScheduleRepository, times(1)).findByConcertIdAndIsSoldOut(concertId, false);
    }

}
