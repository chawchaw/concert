package com.chaw.concert.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetConcerts {
    private final ConcertRepository concertRepository;

    public GetConcerts(ConcertRepository concertRepository) {
        this.concertRepository = concertRepository;
    }

    public Output execute() {
        return Output.builder()
                .concerts(concertRepository.findAll().stream()
                        .map(concert -> Output.ConcertOutput.builder()
                                .id(concert.getId())
                                .name(concert.getName())
                                .info(concert.getInfo())
                                .artist(concert.getArtist())
                                .host(concert.getHost())
                                .build())
                        .toList())
                .build();
    }

    @Builder
    public record Output(
            List<ConcertOutput> concerts
    ) {
        @Builder
        public record ConcertOutput(
                Long id,
                String name,
                String info,
                String artist,
                String host
        ) {
        }
    }
}
