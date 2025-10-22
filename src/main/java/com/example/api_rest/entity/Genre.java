package com.example.api_rest.entity;

public enum Genre {
    HORROR,
    ACTION,
    PLATFORM,
    RPG,
    STRATEGY,
    RACING,
    SANDBOX,
    ADVENTURE;

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
