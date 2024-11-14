package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcertSchedulesNotSoldOutUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetConcertSchedulesNotSoldOutUseCaseUnitTest {

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private ConcertScheduleRepository concertScheduleRepository;

    @InjectMocks
    private GetConcertSchedulesNotSoldOutUseCase getConcertSchedulesNotSoldOutUseCase;

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

        when(concertRepository.findByIdOrThrow(concertId)).thenReturn(concert);

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

        LocalDateTime dateConcertFrom = LocalDateTime.now();
        LocalDateTime dateConcertTo = LocalDateTime.now().plusDays(3);
        when(concertScheduleRepository.findByConcertIdAndIsSoldOutAndDateConcertBetween(concertId, false, dateConcertFrom, dateConcertTo)).thenReturn(concertSchedules);

        // When
        GetConcertSchedulesNotSoldOutUseCase.Input input = new GetConcertSchedulesNotSoldOutUseCase.Input(0L, concertId, dateConcertFrom, dateConcertTo);
        GetConcertSchedulesNotSoldOutUseCase.Output output = getConcertSchedulesNotSoldOutUseCase.execute(input);

        // Then
        assertNotNull(output);
        assertEquals(concertId, output.id());
        assertEquals("Test Concert", output.name());
        assertEquals("Test Concert Info", output.info());
        assertEquals("Test Artist", output.artist());
        assertEquals("Test Host", output.host());

        List<GetConcertSchedulesNotSoldOutUseCase.Output.Item> scheduleItems = output.schedules();
        assertEquals(2, scheduleItems.size());

        GetConcertSchedulesNotSoldOutUseCase.Output.Item firstSchedule = scheduleItems.get(0);
        assertEquals(1L, firstSchedule.id());
        assertEquals(false, firstSchedule.isSoldOut());
        assertEquals(100, firstSchedule.totalSeat());
        assertEquals(50, firstSchedule.availableSeat());

        GetConcertSchedulesNotSoldOutUseCase.Output.Item secondSchedule = scheduleItems.get(1);
        assertEquals(2L, secondSchedule.id());
        assertEquals(false, secondSchedule.isSoldOut());
        assertEquals(200, secondSchedule.totalSeat());
        assertEquals(100, secondSchedule.availableSeat());

        verify(concertRepository, times(1)).findByIdOrThrow(concertId);
        verify(concertScheduleRepository, times(1)).findByConcertIdAndIsSoldOutAndDateConcertBetween(concertId, false, dateConcertFrom, dateConcertTo);
    }

}
