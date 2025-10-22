package com.example.api_rest.service;

import com.example.api_rest.dto.DeleteUserDTO;
import com.example.api_rest.dto.EditUserDTO;
import com.example.api_rest.dto.LoginUserDTO;
import com.example.api_rest.dto.RegisterUserDTO;
import com.example.api_rest.entity.Role;
import com.example.api_rest.entity.User;
import com.example.api_rest.exception.UserNotFoundException;
import com.example.api_rest.exception.UsernameAlreadyExistsException;
import com.example.api_rest.repository.UserRepository;
import com.example.api_rest.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loadUserByUsernameTest_validData() {
        User user = new User(1L, "Pepito", "123");
        when(repository.findByUsername(user.getUsername()))
                .thenReturn(Optional.of(user));

        UserDetails userDetails = authService.loadUserByUsername(user.getUsername());

        assertAll("UserDetails tests",
                () -> assertNotNull(userDetails, "Objeto nulo"),
                () -> assertEquals("Pepito", userDetails.getUsername(), "Username no coincide"),
                () -> assertEquals("123", userDetails.getPassword(), "Password no coincide"));
        verify(repository).findByUsername(userDetails.getUsername());
    }

    @Test
    void loadUserByUsernameTest_invalidData() {
        when(repository.findByUsername("juan"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> authService.loadUserByUsername("juan"));
        verify(repository).findByUsername("juan");
    }

    @Test
    void loginTest_validData() throws Exception{
        LoginUserDTO dto = new LoginUserDTO("Antonio", "abc");
        Authentication authentication = new UsernamePasswordAuthenticationToken(dto.username(), dto.password());

        when(authenticationConfiguration.getAuthenticationManager())
                .thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenService.generateToken(authentication))
                .thenReturn("TOKEN");

        String result = authService.login(dto);

        assertAll(
                () -> assertNotNull(result, "Resultado nulo"),
                () -> assertEquals("TOKEN", result)
        );

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());

        Authentication capturedAuth = captor.getValue();
        assertEquals(dto.username(), capturedAuth.getName());
        assertEquals(dto.password(), capturedAuth.getCredentials());

        verify(tokenService).generateToken(authentication);
    }

    @Test
    void loginTest_invalidData() throws Exception{
        LoginUserDTO badDto = new LoginUserDTO("Jorge", "12445");

        when(authenticationConfiguration.getAuthenticationManager())
                .thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        assertThrows(BadCredentialsException.class,
                () -> authService.login(badDto));
    }

    @Test
    void registerTest_validData() {
        RegisterUserDTO dto = new RegisterUserDTO("mockUser", "1234", null);
        User newUser = new User(9L, dto.username(), "encodedPassword");
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encodedPassword");
        when(repository.findByUsername(dto.username()))
                .thenReturn(Optional.empty());
        when(repository.save(any(User.class)))
                .thenReturn(newUser);

        User result = authService.register(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());

        User captUser = captor.getValue();

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(dto.username(), captUser.getUsername()),
                () -> assertEquals(captUser.getUsername(), result.getUsername()),
                () -> assertEquals("encodedPassword", captUser.getPassword()),
                () -> assertEquals(captUser.getPassword(), result.getPassword())
        );
    }

    @Test
    void registerTest_duplicateUsername() {
        RegisterUserDTO dto = new RegisterUserDTO("mockUser", "1234", null);
        User user = new User(7L, dto.username(), "pwd");
        when(repository.findByUsername(dto.username()))
                .thenReturn(Optional.of(user));

        assertThrows(UsernameAlreadyExistsException.class,
                () -> authService.register(dto));

        verifyNoInteractions(passwordEncoder);
        verify(repository).findByUsername(dto.username());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void editTest_validData() {
        EditUserDTO dto = new EditUserDTO("mockUserMod", "123mod", null, Role.ADMIN);
        User originalUser = new User(1L, "mockUser", "123");

        when(repository.findByUsername(dto.username()))
                .thenReturn(Optional.of(originalUser));
        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode(dto.password()))
                .thenReturn(dto.password() + "Encoded");

        User result = authService.edit(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());

        User captUser = captor.getValue();

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(originalUser.getId(), captUser.getId()),
                () -> assertEquals(dto.password() + "Encoded", captUser.getPassword()),
                () -> assertEquals(dto.role(), captUser.getRole())
        );
    }

    @Test
    void editTest_validData_noPasswordChange() {
        EditUserDTO dto = new EditUserDTO("mockUserMod", "", null, Role.USER);
        User originalUser = new User(1L, "mockUser", "123");

        when(repository.findByUsername(dto.username()))
                .thenReturn(Optional.of(originalUser));
        when(repository.save(any(User.class)))
                .thenReturn(new User());

        User result = authService.edit(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());

        User captUser = captor.getValue();

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(originalUser.getPassword(), captUser.getPassword())
        );
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void editTest_invalidData() {
        EditUserDTO dto = new EditUserDTO(null, null, null, null);

        when(repository.findByUsername(dto.username()))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> authService.edit(dto));
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void deleteTest_withIdAndUsername() {
        DeleteUserDTO dto = new DeleteUserDTO(8L, "Manolito");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("Juanito", "123"));
        User targetUser = new User(8L, "Pepito", "1234");
        when(repository.findById(dto.id()))
                .thenReturn(Optional.of(targetUser));

        authService.delete(dto);

        verify(repository).delete(targetUser);
        verify(repository, never()).findByUsername(anyString());
    }

    @Test
    void deleteTest_nullId_notNullUsername() {
        DeleteUserDTO dto = new DeleteUserDTO(null, "Manolito");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("Juanito", "123"));
        User targetUser = new User(11L, "Manolito", "1234");
        when(repository.findByUsername(dto.username()))
                .thenReturn(Optional.of(targetUser));

        authService.delete(dto);

        verify(repository).delete(targetUser);
        verify(repository, never()).findById(anyLong());
    }

    @Test
    void deleteTest_nullIdAndUsername() {
        DeleteUserDTO dto = new DeleteUserDTO(null, null);

        assertThrows(IllegalArgumentException.class, () -> authService.delete(dto));

        verify(repository, never()).findById(dto.id());
        verify(repository, never()).findByUsername(dto.username());
        verify(repository, never()).delete(any(User.class));
    }

    @Test
    void deleteTest_userNotFound() {
        DeleteUserDTO dto = new DeleteUserDTO(8L, "Manolito");
        when(repository.findById(dto.id()))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.delete(dto));

        verify(repository).findById(dto.id());
        verify(repository, never()).delete(any(User.class));
    }

    @Test
    void deleteTest_tryDeleteItself() {
        DeleteUserDTO dto = new DeleteUserDTO(11L, "Manolito");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("Manolito", "123"));
        User targetUser = new User(11L, "Manolito", "123");
        when(repository.findById(dto.id()))
                .thenReturn(Optional.of(targetUser));

        assertThrows(IllegalArgumentException.class, () -> authService.delete(dto));

        verify(repository).findById(dto.id());
        verify(repository, never()).delete(any(User.class));
    }

    @Test
    void validateTokenTest() {
        String token = "Token";

        when(tokenService.validateToken(token))
                .thenReturn(Boolean.TRUE);

        assertTrue(authService.validateToken(token));

        verify(tokenService).validateToken(token);
    }

    @Test
    void getUserFromTokenTest() {
        String token = "Token";
        String username = "Antonio";

        when(tokenService.getUserFromToken(token))
                .thenReturn(username);

        assertEquals(username, authService.getUserFromToken(token));

        verify(tokenService).getUserFromToken(token);
    }

    @Test
    void getAuthoritiesFromTokenTest() {
        String token = "Token";
        List<GrantedAuthority> expectedList = Collections.emptyList();

        when(tokenService.getAuthoritiesFromToken(token))
                .thenReturn(expectedList);

        assertEquals(expectedList, authService.getAuthoritiesFromToken(token));

        verify(tokenService).getAuthoritiesFromToken(token);
    }
}
