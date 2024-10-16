package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetConcerts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class GetConcertsIT {

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private GetConcerts getConcerts;

    @BeforeEach
    void setUp() {
        Concert concert1 = Concert.builder().name("Concert 1").build();
        Concert concert2 = Concert.builder().name("Concert 2").build();

        concertRepository.save(concert1);
        concertRepository.save(concert2);
    }

    @Test
    void testGetConcerts() {
        // When
        GetConcerts.Output output = getConcerts.execute();

        // Then
        List<Concert> concerts = output.concerts();
        assertEquals(2, concerts.size());
        assertTrue(concerts.stream().anyMatch(c -> c.getName().equals("Concert 1")));
        assertTrue(concerts.stream().anyMatch(c -> c.getName().equals("Concert 2")));
    }
}
