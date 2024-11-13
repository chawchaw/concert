package com.chaw.concert.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.repository.ActiveTokenRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitTokenRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 대기열에 입장 및 대기순서, 상태 조회
 */
@Slf4j
@AllArgsConstructor
@Service
public class EnterWaitTokenUseCase {

    private final ActiveTokenRepository activeTokenRepository;
    private final WaitTokenRepository waitTokenRepository;

    public Output execute(Input input) {
        Boolean isActive = activeTokenRepository.existsByUserId(input.userId());
        if (isActive) {
            return new Output("ACTIVE", -1);
        }

        Integer rank = waitTokenRepository.getRankByUserId(input.userId());
        if (rank == null) {
            waitTokenRepository.saveWithCurrentTime(input.userId());
            rank = waitTokenRepository.getRankByUserId(input.userId());
            log.info("대기열 입장");
        }

        return new Output("WAIT", rank);
    }

    public record Input (
        Long userId
    ) {}

    public record Output (
        String status,
        Integer order
    ) {}
}
