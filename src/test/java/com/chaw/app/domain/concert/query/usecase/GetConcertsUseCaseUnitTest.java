package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcertsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetConcertsUseCaseUnitTest {
    @Mock
    private ConcertRepository concertRepository;

    @InjectMocks
    private GetConcertsUseCase getConcertsUseCase;

    @Test
    void testExecute() {
        // Given
        Concert concert1 = Concert.builder().id(1L).build();
        Concert concert2 = Concert.builder().id(2L).build();
        List<Concert> mockConcerts = Arrays.asList(concert1, concert2);

        when(concertRepository.findAll()).thenReturn(mockConcerts);

        // When
        GetConcertsUseCase.Output output = getConcertsUseCase.execute();

        // Then
        assertEquals(2, output.concerts().size());
        assertEquals(1L, output.concerts().get(0).id());
        assertEquals(2L, output.concerts().get(1).id());
    }
}
