package com.example.api_rest.controller;

import com.example.api_rest.config.ApiConfig;
import com.example.api_rest.dto.VideoGameDTO;
import com.example.api_rest.entity.VideoGame;
import com.example.api_rest.exception.VideoGameNotFoundException;
import com.example.api_rest.service.VideoGameService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(path = ApiConfig.API_BASE_PATH + "/games")
public class VideoGameController {

    private final VideoGameService videoGameService;

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoGameController.class);

    public VideoGameController(VideoGameService videoGameService) {
        this.videoGameService = videoGameService;
    }

    @GetMapping
    public ResponseEntity<List<VideoGame>> findAll() {
        return ResponseEntity.ok(videoGameService.findAll());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<VideoGame> findById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(videoGameService.findById(id));
    }

    @PostMapping
    public ResponseEntity<VideoGame> register(@Valid @RequestBody VideoGameDTO dto) {
        VideoGame videoGame = videoGameService.register(dto);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(videoGame.getId())
                .toUri();

        return ResponseEntity.created(uri).body(videoGame);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VideoGame> update(@PathVariable Long id, @Valid @RequestBody VideoGameDTO dto) {
        return ResponseEntity.ok(videoGameService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        videoGameService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        LOGGER.info("Request to delete all video games");
        videoGameService.deleteAll();

        return ResponseEntity.noContent().build();
    }
}
