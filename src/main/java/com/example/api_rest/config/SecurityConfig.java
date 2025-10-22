package com.example.api_rest.config;

import com.example.api_rest.entity.Role;
import com.example.api_rest.exception.handler.CustomSecurityExceptionHandler;
import com.example.api_rest.filter.JwtAuthenticationFilter;
import com.example.api_rest.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig {

    public static final String API_BASE_AUTH = ApiConfig.API_BASE_PATH + "/auth";

    private final Environment environment;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtFilter;
    private final CustomSecurityExceptionHandler customSecurityExceptionHandler;

    public SecurityConfig(Environment environment, PasswordEncoder passwordEncoder, UserDetailsService userDetailsService, AuthService authService, JwtAuthenticationFilter jwtFilter, CustomSecurityExceptionHandler customSecurityExceptionHandler) {
        this.environment = environment;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
        this.customSecurityExceptionHandler = customSecurityExceptionHandler;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String jwtCookieName = environment.getProperty("app.jwt.cookie-name");
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtFilter, LogoutFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customSecurityExceptionHandler)
                        .accessDeniedHandler(customSecurityExceptionHandler))
                .authorizeHttpRequests(request ->
                        request
                                .requestMatchers("/docs/**").permitAll()
                                .requestMatchers("/swagger-ui.html").permitAll()
                                .requestMatchers("/v3/api-docs").permitAll()
                                .requestMatchers(API_BASE_AUTH + "/login").permitAll()
                                .requestMatchers(API_BASE_AUTH + "/*").hasRole(Role.ADMIN.name())
                                .requestMatchers(HttpMethod.GET, ApiConfig.API_BASE_PATH + "/games").hasRole(Role.USER.name())
                                .requestMatchers(ApiConfig.API_BASE_PATH + "/games").hasRole(Role.ADMIN.name())
                                .requestMatchers(HttpMethod.GET, ApiConfig.API_BASE_PATH + "/games/*").hasRole(Role.USER.name())
                                .requestMatchers(ApiConfig.API_BASE_PATH + "/games/*").hasRole(Role.ADMIN.name())
                                .anyRequest().denyAll())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .logout(logout -> logout
                        .logoutRequestMatcher(request ->
                                request.getRequestURI().equals(API_BASE_AUTH + "/logout") && request.getMethod().equals("POST"))
                        .logoutSuccessHandler(((request, response, authentication) -> {
                            final Cookie cookie = new Cookie(jwtCookieName, null);
                            cookie.setMaxAge(0);
                            cookie.setPath(ApiConfig.API_BASE_PATH);
                            response.addCookie(cookie);

                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        }))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies(jwtCookieName, "JSESSIONID"))
                .formLogin(FormLoginConfigurer::disable);
        return http.build();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_USER");
    }

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }
}
