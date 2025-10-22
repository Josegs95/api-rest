package com.example.api_rest.service;

import com.example.api_rest.service.impl.TokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private TokenServiceImpl tokenService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(tokenService, "jwtKeyExpirationTime", 30);
    }

    @Test
    void generateTokenTest() {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "Manuel",
                        "4321",
                        List.of(new SimpleGrantedAuthority("ADMIN"))
                );
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwt);
        when(jwt.getTokenValue())
                .thenReturn("TOKEN");

        String result = tokenService.generateToken(authentication);

        assertAll(
                () -> assertNotNull(result, "Token nulo"),
                () -> assertEquals("TOKEN", result, "Token incorrecto")
        );

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();

        assertAll(
                () -> assertEquals("Manuel", claims.getSubject(), "Sujeto incorrecto"),
                () -> assertEquals("ADMIN", claims.getClaim("roles"), "Roles incorrectos")
        );
    }

    @Test
    void validateTokenTest_validData() {
        when(jwtDecoder.decode("TOKEN"))
                .thenReturn(jwt);

        assertTrue(tokenService.validateToken("TOKEN"));
    }

    @Test
    void validateTokenTest_invalidData() {
        when(jwtDecoder.decode("INVALID"))
                .thenThrow(new BadJwtException("Invalid token"));

        assertThrows(BadJwtException.class,
                () -> tokenService.validateToken("INVALID"));
    }

    @Test
    void getAuthoritiesFromTokenTest_singleRole() {
        when(jwtDecoder.decode("TOKEN"))
                .thenReturn(jwt);
        when(jwt.getClaimAsString("roles"))
                .thenReturn("USER");

        List<GrantedAuthority> expectedList = List.of(
                new SimpleGrantedAuthority("USER")
        );

        assertEquals(expectedList, tokenService.getAuthoritiesFromToken("TOKEN"));
    }

    // En la API un usuario solo puede tener un rol, pero hago este test para añadir robustez a la
    // aplicación en caso de datos corruptos por ejemplo.

    @Test
    void getAuthoritiesFromTokenTest_multipleRoles() {
        when(jwtDecoder.decode("TOKEN"))
                .thenReturn(jwt);
        when(jwt.getClaimAsString("roles"))
                .thenReturn("USER ADMIN");

        List<GrantedAuthority> expectedList = List.of(
                new SimpleGrantedAuthority("USER"),
                new SimpleGrantedAuthority("ADMIN")
        );

        assertEquals(expectedList, tokenService.getAuthoritiesFromToken("TOKEN"));
    }
}
