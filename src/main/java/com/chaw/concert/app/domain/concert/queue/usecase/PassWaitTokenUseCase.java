package com.chaw.concert.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.WaitToken;
import com.chaw.concert.app.domain.concert.queue.repository.ActiveTokenRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 통과 스케줄러
 */
@AllArgsConstructor
@Service
public class PassWaitTokenUseCase {

    private static final int PASS_SIZE = 30;
    private static final int TTL_ACTIVE_TOKEN_SECONDS = 60 * 10;

    private final WaitTokenRepository waitTokenRepository;
    private final ActiveTokenRepository activeTokenRepository;

    public Output execute() {
        List<WaitToken> tokens = waitTokenRepository.getTokensEligibleForPass(PASS_SIZE);
        if (tokens.isEmpty()) {
            return new Output(0);
        }
        waitTokenRepository.removeTokensBeforeTimeStamp(tokens.get(tokens.size() - 1).score());
        tokens.forEach(t -> {
            Long userId = Long.parseLong(t.key());
            activeTokenRepository.save(userId, TTL_ACTIVE_TOKEN_SECONDS);
        });
        return new Output(tokens.size());
    }

    public record Output (
        Integer countPass
    ) {}
}
