package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcertSchedulesNotSoldOut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class GetConcertSchedulesNotSoldOutUnitTest {

    @Mock
    private ConcertScheduleRepository concertScheduleRepository;

    @InjectMocks
    private GetConcertSchedulesNotSoldOut getConcertSchedulesNotSoldOut;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mock 객체 초기화
    }

    @Test
    void testGetConcertSchedulesNotSoldOut() {
        // Given
        Long concertId = 1L;
        List<ConcertSchedule> mockSchedules = Arrays.asList(
                new ConcertSchedule(/* mock 데이터 */),
                new ConcertSchedule(/* mock 데이터 */)
        );

        when(concertScheduleRepository.findByConcertIdAndIsSold(concertId, false)).thenReturn(mockSchedules);

        // When
        GetConcertSchedulesNotSoldOut.Input input = new GetConcertSchedulesNotSoldOut.Input(concertId);
        GetConcertSchedulesNotSoldOut.Output output = getConcertSchedulesNotSoldOut.execute(input);

        // Then
        assertEquals(2, output.getConcertSchedules().size()); // 예상된 결과 확인
    }
}
