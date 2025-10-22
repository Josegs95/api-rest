package com.example.api_rest.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "api_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    public User() {
    }

    public User(String username, String password) {
        this(username, password, null, LocalDateTime.now(), LocalDateTime.now(), null);
    }

    public User(Long id, String username, String password) {
        this(username, password, null, LocalDateTime.now(), LocalDateTime.now(), null);
        setId(id);
    }

    public User(String username, String password, String email, LocalDateTime registrationDate, LocalDateTime updateDate, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.registrationDate = registrationDate;
        this.updateDate = updateDate;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName()).append(" [");
        sb.append("Username = ").append(this.username).append(", ");
        sb.append("Password = [PROTECTED], ");
        sb.append("Email = ").append(this.email).append(", ");
        sb.append("Registration_Date = ").append(this.registrationDate).append(", ");
        sb.append("Update_Date = ").append(this.updateDate).append(", ");
        sb.append("Role = ").append(this.role).append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof User user)) return false;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }
}
