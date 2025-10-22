package com.example.api_rest.service;

import com.example.api_rest.dto.VideoGameDTO;
import com.example.api_rest.entity.VideoGame;

import java.util.List;

public interface VideoGameService {
    List<VideoGame> findAll();
    VideoGame findById(Long id);
    VideoGame register(VideoGameDTO dto);
    VideoGame update(Long id, VideoGameDTO dto);
    void delete(Long id);
    void deleteAll();
}
