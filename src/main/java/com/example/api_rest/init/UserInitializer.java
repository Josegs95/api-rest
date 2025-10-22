package com.example.api_rest.init;

import com.example.api_rest.entity.Role;
import com.example.api_rest.entity.User;
import com.example.api_rest.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class UserInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserInitializer.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserInitializer(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if(repository.findByUsername("admin").isEmpty()) {
            User admin = new User("admin", passwordEncoder.encode("12345"), "admin@gmail.com",
                    LocalDateTime.now(), LocalDateTime.now(), Role.ADMIN);
            repository.save(admin);
            LOGGER.info("Admin creado exitosamente");
        }

        if(repository.findByUsername("user").isEmpty()) {
            User user = new User("user", passwordEncoder.encode("54321"), "user@yahoo.es",
                    LocalDateTime.now(), LocalDateTime.now(), Role.USER);
            repository.save(user);
            LOGGER.info("User creado exitosamente");
        }
    }
}
