package com.chaw.concert.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetConcerts {
    private final ConcertRepository concertRepository;

    public GetConcerts(ConcertRepository concertRepository) {
        this.concertRepository = concertRepository;
    }

    public Output execute() {
        return new Output(concertRepository.findAll());
    }

    public record Output(
            List<Concert> concerts
    ) {}
}
