package com.chaw.concert.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.UserNode;
import com.chaw.concert.app.domain.concert.queue.repository.UserNodeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 통과 스케줄러
 */
@AllArgsConstructor
@Service
public class ActiveUserNodesUseCase {

    private final UserNodeRepository userNodeRepository;

    public Output execute() {
        List<UserNode> userNodes = userNodeRepository.findTopWait(UserNode.PASS_SIZE);

        userNodeRepository.activeUserNodes(userNodes);

        return new Output(userNodes.size());
    }

    public record Output (
        Integer countPass
    ) {}
}
