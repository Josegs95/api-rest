package com.example.api_rest.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "api_video_game")
public class VideoGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "developed_by")
    private String developedBy;

    @Enumerated(value = EnumType.STRING)
    private Genre genre;

    public VideoGame() {
    }

    public VideoGame(String name) {
        this(name, LocalDate.now(), null, null);
    }

    public VideoGame(Long id, String name) {
        this(name, LocalDate.now(), null, null);
        setId(id);
    }

    public VideoGame(String name, LocalDate releaseDate, String developedBy, Genre genre) {
        this.name = name;
        this.releaseDate = releaseDate;
        this.developedBy = developedBy;
        this.genre = genre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDevelopedBy() {
        return developedBy;
    }

    public void setDevelopedBy(String developedBy) {
        this.developedBy = developedBy;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }
}
