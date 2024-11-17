package com.chaw.concert.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.UserNode;
import com.chaw.concert.app.domain.concert.queue.entity.UserNodeStatus;
import com.chaw.concert.app.domain.concert.queue.repository.UserNodeRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 대기열에 입장 및 대기순서, 상태 조회
 */
@Slf4j
@AllArgsConstructor
@Service
public class GetUserNodeUseCase {

    private final UserNodeRepository userNodeRepository;

    public Output execute(Input input) {
        UserNode userNode = userNodeRepository.findByUserId(input.userId());

        if (userNode == null) {
            userNode = userNodeRepository.createWait(input.userId());
        }

        return new Output(userNode.getStatus(), userNode.getRank());
    }

    public record Input (
        Long userId
    ) {}

    public record Output (
        UserNodeStatus status,
        Integer order
    ) {}
}
