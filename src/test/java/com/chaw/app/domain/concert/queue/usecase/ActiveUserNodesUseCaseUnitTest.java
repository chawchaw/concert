package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.UserNode;
import com.chaw.concert.app.domain.concert.queue.entity.UserNodeStatus;
import com.chaw.concert.app.domain.concert.queue.repository.UserNodeRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.ActiveUserNodesUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActiveUserNodesUseCaseUnitTest {

    @Mock
    private UserNodeRepository userNodeRepository;

    @InjectMocks
    private ActiveUserNodesUseCase activeUserNodesUseCase;

    @Test
    void test_pass_all() {
        // Given
        List<UserNode> mockUserNodes = Arrays.asList(
                UserNode.builder().userId(1L).rank(1).status(UserNodeStatus.WAIT).build(),
                UserNode.builder().userId(1L).rank(2).status(UserNodeStatus.WAIT).build()
//                new UserNode("1", 1.0, UserNodeStatus.WAIT),
//                new UserNode("1", 2.0, UserNodeStatus.WAIT)
        );
        when(userNodeRepository.findTopWait(anyInt())).thenReturn(mockUserNodes);
        doNothing().when(userNodeRepository).activeUserNodes(mockUserNodes);

        // When
        ActiveUserNodesUseCase.Output result = activeUserNodesUseCase.execute();

        // Then
        assertEquals(2, result.countPass());

    }

}
