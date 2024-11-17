package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.queue.entity.UserNode;
import com.chaw.concert.app.domain.concert.queue.entity.UserNodeStatus;
import com.chaw.concert.app.domain.concert.queue.repository.UserNodeRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.GetUserNodeUseCase;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;

import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ConcertApplication.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class GetUserNodeUseCaseIT {

    @Autowired
    private UserNodeRepository userNodeRepository;

    @Autowired
    private GetUserNodeUseCase getUserNodeUseCase;

    @Test
    void test_status_in_EMPTY() {
        // When
        Long userId = 1L;

        // Then
        GetUserNodeUseCase.Input input = new GetUserNodeUseCase.Input(userId);
        GetUserNodeUseCase.Output output = getUserNodeUseCase.execute(input);

        // Verify
        assertEquals(UserNodeStatus.WAIT, output.status());
        assertEquals(0, output.order());
    }

    @Test
    void test_empty_in_ACTIVE() {
        // When
        Long userId = 1L;
        userNodeRepository.createActive(userId);
        GetUserNodeUseCase.Input input = new GetUserNodeUseCase.Input(userId);
        GetUserNodeUseCase.Output output = getUserNodeUseCase.execute(input);

        // Then
        assertEquals(UserNodeStatus.ACTIVE, output.status());
    }

    @Test
    void test_empty_in_WAIT() {
        // When
        Long userId = 1L;
        userNodeRepository.createWait(userId);
        GetUserNodeUseCase.Input input = new GetUserNodeUseCase.Input(userId);
        GetUserNodeUseCase.Output output = getUserNodeUseCase.execute(input);

        // Then
        assertEquals(UserNodeStatus.WAIT, output.status());
        assertEquals(0, output.order());
    }

    @Test
    void test_multi_user_enter() {
        // When
        LongStream.rangeClosed(1, 10).forEach(i -> {
            getUserNodeUseCase.execute(new GetUserNodeUseCase.Input(Long.valueOf(i)));
        });

        // Then
        Long userId = 5L;
        long countAll = userNodeRepository.countByStatus(UserNodeStatus.WAIT);
        UserNode userNode5 = userNodeRepository.findByUserId(userId);
        assertEquals(10, countAll);
        assertEquals(5 - 1, userNode5.getRank());
    }
}
