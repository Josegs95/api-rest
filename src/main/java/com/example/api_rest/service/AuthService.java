package com.example.api_rest.service;

import com.example.api_rest.dto.DeleteUserDTO;
import com.example.api_rest.dto.EditUserDTO;
import com.example.api_rest.dto.LoginUserDTO;
import com.example.api_rest.dto.RegisterUserDTO;
import com.example.api_rest.entity.User;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public interface AuthService {
    User register(RegisterUserDTO dto);
    String login(LoginUserDTO dto) throws BadCredentialsException;
    User edit(EditUserDTO dto);
    void delete(DeleteUserDTO dto);
    boolean validateToken(String token);
    String getUserFromToken(String token);
    List<GrantedAuthority> getAuthoritiesFromToken(String token);
}
