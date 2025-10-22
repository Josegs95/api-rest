package com.example.api_rest.service;

import com.example.api_rest.dto.VideoGameDTO;
import com.example.api_rest.entity.VideoGame;
import com.example.api_rest.exception.VideoGameNotFoundException;
import com.example.api_rest.repository.VideoGameRepository;
import com.example.api_rest.service.impl.VideoGameServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VideoGameServiceTest {

    @Mock
    private VideoGameRepository repository;

    @InjectMocks
    private VideoGameServiceImpl service;

    @Test
    void findAllTest_withData() {
        List<VideoGame> expectedList = List.of(
                new VideoGame("Call of Duty"),
                new VideoGame("Fornite"),
                new VideoGame("Assassin Creed")
        );
        when(repository.findAll())
                .thenReturn(expectedList);

        List<VideoGame> result = service.findAll();
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(expectedList.size(), result.size()),
                () -> assertEquals(expectedList.get(1).getName(), result.get(1).getName())
        );
        verify(repository).findAll();
    }

    @Test
    void findAllTest_noData() {
        List<VideoGame> expectedList = Collections.emptyList();
        when(repository.findAll())
                .thenReturn(expectedList);

        List<VideoGame> result = service.findAll();
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(expectedList.size(), result.size())
        );
        verify(repository).findAll();
    }

    @Test
    void findByIdTest_validData() {
        Long id = 99L;
        VideoGame videoGame = new VideoGame(id, "MockName");

        when(repository.findById(id))
                .thenReturn(Optional.of(videoGame));

        VideoGame result = service.findById(id);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(videoGame, result)
        );
    }

    @Test
    void findByIdTest_invalidData() {
        Long id = 99L;

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(VideoGameNotFoundException.class, () -> service.findById(id));
    }

    @Test
    void registerTest() {
        VideoGameDTO dto = new VideoGameDTO("mockName", LocalDate.now(), "mockDeveloper", null);
        VideoGame savedVideoGame = new VideoGame(1L, dto.name());

        when(repository.save(any(VideoGame.class)))
                .thenReturn(savedVideoGame);

        VideoGame result = service.register(dto);

        ArgumentCaptor<VideoGame> captor = ArgumentCaptor.forClass(VideoGame.class);
        verify(repository).save(captor.capture());

        VideoGame captVideoGame = captor.getValue();

        assertAll(
                () -> assertEquals(dto.name(), captVideoGame.getName()),
                () -> assertEquals(dto.releaseDate(), captVideoGame.getReleaseDate()),
                () -> assertEquals(dto.developedBy(), captVideoGame.getDevelopedBy()),
                () -> assertEquals(dto.genre(), captVideoGame.getGenre()),
                () -> assertSame(savedVideoGame, result)
        );
    }

    @Test
    void updateTest_validData() {
        Long id = 16L;
        VideoGameDTO dto = new VideoGameDTO("mockName", LocalDate.now(), "mockDeveloper", null);
        VideoGame videoGame = new VideoGame(id, "oldMockName");

        when(repository.findById(id))
                .thenReturn(Optional.of(videoGame));
        when(repository.save(videoGame))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VideoGame result = service.update(id, dto);

        assertAll(
                () -> assertEquals(dto.name(), videoGame.getName()),
                () -> assertEquals(dto.releaseDate(), videoGame.getReleaseDate()),
                () -> assertEquals(dto.developedBy(), videoGame.getDevelopedBy()),
                () -> assertEquals(dto.genre(), videoGame.getGenre()),
                () -> assertSame(videoGame, result)
        );
        verify(repository).save(videoGame);
    }

    @Test
    void updateTest_invalidData() {
        Long id = 99L;
        VideoGameDTO dto = new VideoGameDTO("mockName", LocalDate.now(), "mockDeveloper", null);

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(VideoGameNotFoundException.class, () -> service.update(id, dto));
        verify(repository, never()).save(any(VideoGame.class));
    }

    @Test
    void deleteTest_validData() {
        Long id = 13L;
        VideoGame videoGame = new VideoGame(id, "mockName");

        when(repository.findById(id))
                .thenReturn(Optional.of(videoGame));

        service.delete(id);

        verify(repository).delete(videoGame);
    }

    @Test
    void deleteTest_invalidData() {
        Long id = 99L;

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(VideoGameNotFoundException.class, () -> service.delete(id));

        verify(repository, never()).delete(any(VideoGame.class));
    }

    @Test
    void deleteAllTest() {
        service.deleteAll();

        verify(repository).deleteAll();
        verifyNoMoreInteractions(repository);
    }
}
