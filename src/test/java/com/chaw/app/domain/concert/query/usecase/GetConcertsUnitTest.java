package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcerts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class GetConcertsUnitTest {
    @Mock
    private ConcertRepository concertRepository;

    @InjectMocks
    private GetConcerts getConcerts;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecute() {
        // Given
        Concert concert1 = new Concert();
        Concert concert2 = new Concert();
        List<Concert> mockConcerts = Arrays.asList(concert1, concert2);

        when(concertRepository.findAll()).thenReturn(mockConcerts);

        // When
        GetConcerts.Output output = getConcerts.execute();

        // Then
        assertEquals(mockConcerts, output.concerts());
    }
}
