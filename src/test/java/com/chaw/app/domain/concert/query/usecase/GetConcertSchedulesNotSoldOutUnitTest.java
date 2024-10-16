package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.exception.ConcertNotFound;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcertSchedulesNotSoldOut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
    void testExecute_ConcertNotFound() {
        // Given
        Long concertId = 1L;
        when(concertRepository.existsById(concertId)).thenReturn(false);

        // When / Then
        GetConcertSchedulesNotSoldOut.Input input = new GetConcertSchedulesNotSoldOut.Input(concertId);
        assertThrows(ConcertNotFound.class, () -> getConcertSchedulesNotSoldOut.execute(input));

        verify(concertRepository, times(1)).existsById(concertId);
        verify(concertScheduleRepository, never()).findByConcertIdAndIsSold(anyLong(), anyBoolean());
    }

    @Test
    void testExecute_ConcertFound() {
        // Given
        Long concertId = 1L;
        when(concertRepository.existsById(concertId)).thenReturn(true);

        List<ConcertSchedule> concertSchedules = Arrays.asList(
                new ConcertSchedule(),
                new ConcertSchedule()
        );
        when(concertScheduleRepository.findByConcertIdAndIsSold(concertId, false)).thenReturn(concertSchedules);

        // When
        GetConcertSchedulesNotSoldOut.Input input = new GetConcertSchedulesNotSoldOut.Input(concertId);
        GetConcertSchedulesNotSoldOut.Output output = getConcertSchedulesNotSoldOut.execute(input);

        // Then
        assertNotNull(output);
        assertEquals(2, output.concertSchedules().size());

        verify(concertRepository, times(1)).existsById(concertId);
        verify(concertScheduleRepository, times(1)).findByConcertIdAndIsSold(concertId, false);
    }
}
