package com.example.api_rest.service.impl;

import com.example.api_rest.dto.VideoGameDTO;
import com.example.api_rest.entity.VideoGame;
import com.example.api_rest.exception.VideoGameNotFoundException;
import com.example.api_rest.repository.VideoGameRepository;
import com.example.api_rest.service.VideoGameService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoGameServiceImpl implements VideoGameService {

    private final VideoGameRepository repository;

    public VideoGameServiceImpl(VideoGameRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<VideoGame> findAll() {
        return repository.findAll();
    }

    @Override
    public VideoGame findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new VideoGameNotFoundException("It does not exist a video game with id: " + id));
    }

    @Override
    public VideoGame register(VideoGameDTO dto) {
        VideoGame videoGame = new VideoGame(
                dto.name(),
                dto.releaseDate(),
                dto.developedBy(),
                dto.genre());

        return repository.save(videoGame);
    }

    @Override
    public VideoGame update(Long id, VideoGameDTO dto) {
        VideoGame videoGame = repository.findById(id)
                .orElseThrow(() -> new VideoGameNotFoundException("It does not exist a video game with id: " + id));
        videoGame.setName(dto.name());
        videoGame.setReleaseDate(dto.releaseDate());
        videoGame.setDevelopedBy(dto.developedBy());
        videoGame.setGenre(dto.genre());

        return repository.save(videoGame);
    }

    @Override
    public void delete(Long id) {
        VideoGame videoGame = repository.findById(id)
                .orElseThrow(() -> new VideoGameNotFoundException("It does not exist a video game with id: " + id));
        repository.delete(videoGame);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
}
