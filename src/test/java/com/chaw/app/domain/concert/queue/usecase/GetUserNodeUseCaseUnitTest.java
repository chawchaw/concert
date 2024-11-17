package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.UserNode;
import com.chaw.concert.app.domain.concert.queue.entity.UserNodeStatus;
import com.chaw.concert.app.domain.concert.queue.repository.UserNodeRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.GetUserNodeUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetUserNodeUseCaseUnitTest {

    @Mock
    private UserNodeRepository userNodeRepository;

    @InjectMocks
    private GetUserNodeUseCase getUserNodeUseCase;

    @Test
    void test_status_in_ACTIVE() {
        // Given
        UserNode userNode = UserNode.builder().userId(1L).status(UserNodeStatus.ACTIVE).build();
        when(userNodeRepository.findByUserId(anyLong())).thenReturn(userNode);

        // When
        GetUserNodeUseCase.Input input = new GetUserNodeUseCase.Input(1L);
        GetUserNodeUseCase.Output output = getUserNodeUseCase.execute(input);

        // Then
        verify(userNodeRepository, times(0)).createWait(anyLong());
        assertEquals(UserNodeStatus.ACTIVE, output.status());
    }

    @Test
    void test_status_in_WAIT_entered() {
        // Given
        UserNode userNode = UserNode.builder().userId(1L).rank(1).status(UserNodeStatus.WAIT).build();
        when(userNodeRepository.findByUserId(anyLong())).thenReturn(userNode);

        // When
        GetUserNodeUseCase.Input input = new GetUserNodeUseCase.Input(1L);
        GetUserNodeUseCase.Output output = getUserNodeUseCase.execute(input);

        // Then
        verify(userNodeRepository, times(0)).createWait(anyLong());
        assertEquals(UserNodeStatus.WAIT, output.status());
        assertEquals(1, output.order());
    }

    @Test
    void test_status_in_WAIT_not_entered() {
        // Given
        when(userNodeRepository.findByUserId(anyLong())).thenReturn(null);
        UserNode userNode = UserNode.builder().userId(1L).rank(1).status(UserNodeStatus.WAIT).build();
        when(userNodeRepository.createWait(anyLong())).thenReturn(userNode);

        // When
        GetUserNodeUseCase.Input input = new GetUserNodeUseCase.Input(1L);
        GetUserNodeUseCase.Output output = getUserNodeUseCase.execute(input);

        // Then
        verify(userNodeRepository, times(1)).createWait(anyLong());
        assertEquals(UserNodeStatus.WAIT, output.status());
        assertEquals(1, output.order());
    }
}
