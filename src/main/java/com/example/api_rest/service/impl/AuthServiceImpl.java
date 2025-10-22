package com.example.api_rest.service.impl;

import com.example.api_rest.dto.DeleteUserDTO;
import com.example.api_rest.dto.EditUserDTO;
import com.example.api_rest.dto.LoginUserDTO;
import com.example.api_rest.dto.RegisterUserDTO;
import com.example.api_rest.entity.Role;
import com.example.api_rest.entity.User;
import com.example.api_rest.exception.AuthenticationException;
import com.example.api_rest.exception.UserNotFoundException;
import com.example.api_rest.exception.UsernameAlreadyExistsException;
import com.example.api_rest.repository.UserRepository;
import com.example.api_rest.service.AuthService;
import com.example.api_rest.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService, UserDetailsService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository repository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationConfiguration authenticationConfiguration;

    public AuthServiceImpl(UserRepository repository, TokenService tokenService, PasswordEncoder passwordEncoder, AuthenticationConfiguration authenticationConfiguration) {
        this.repository = repository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationConfiguration = authenticationConfiguration;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByUsername(username).orElseThrow(() -> {
            LOGGER.error("❌ User not found with username: {}", username);
            return new UsernameNotFoundException("User not found");
        });
        return new CustomUserDetails(user);
    }

    @Override
    public User register(RegisterUserDTO dto) {
        if (repository.findByUsername(dto.username()).isPresent()) {
            throw new UsernameAlreadyExistsException("The username is already in use");
        }

        User user = new User(
                dto.username(),
                passwordEncoder.encode(dto.password()),
                dto.email(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                Role.USER
        );

        return repository.save(user);
    }

    @Override
    public String login(LoginUserDTO dto){
        try {
            AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
            Authentication authRequest = new UsernamePasswordAuthenticationToken(
                    dto.username(), dto.password());
            Authentication authentication = authenticationManager.authenticate(authRequest);
            return tokenService.generateToken(authentication);
        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("❌ Error while trying to login", e);
            throw new AuthenticationException("Error while trying to login");
        }
    }

    @Override
    public User edit(EditUserDTO dto) {
        User user = repository.findByUsername(dto.username())
                .orElseThrow(() -> new UserNotFoundException("Attempted to edit a user that does not exist"));

        if (dto.password() != null && !dto.password().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }
        user.setEmail(dto.email());
        user.setRole(dto.role());
        user.setUpdateDate(LocalDateTime.now());

        return repository.save(user);
    }

    @Override
    public void delete(DeleteUserDTO dto) {
        User user = null;
        if (dto.id() != null) {
            user = repository.findById(dto.id())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + dto.id()));
        } else if (dto.username() != null) {
            user = repository.findByUsername(dto.username())
                    .orElseThrow(() -> new UserNotFoundException("User not found with username: " + dto.username()));
        } else {
            throw new IllegalArgumentException("Both id and username fields are null");
        }

        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentUserName.equalsIgnoreCase(user.getUsername())) {
            throw new IllegalArgumentException("You are not allowed to delete your own user account");
        }

        repository.delete(user);
    }

    @Override
    public boolean validateToken(String token) {
        return tokenService.validateToken(token);
    }

    @Override
    public String getUserFromToken(String token) {
        return tokenService.getUserFromToken(token);
    }

    @Override
    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        return tokenService.getAuthoritiesFromToken(token);
    }

    private record CustomUserDetails(User user) implements UserDetails {

        @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority(user.getRole().toString()));
            }

            @Override
            public String getPassword() {
                return user.getPassword();
            }

            @Override
            public String getUsername() {
                return user.getUsername();
            }
        }
}
