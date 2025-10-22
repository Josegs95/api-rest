package com.example.api_rest.entity;

public enum Role{
    USER,
    ADMIN;

    @Override
    public String toString() {
        return "ROLE_" + name();
    }
}
