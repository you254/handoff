package com.handoff.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.handoff.model.enums.UserRole;
import com.handoff.model.enums.UserStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "uk_users_email", columnList = "email", unique = true)
})
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Type(JsonType.class)
    @Column(name = "profile", columnDefinition = "jsonb")
    @JsonIgnore
    private JsonNode profileJson;

    @Type(JsonType.class)
    @Column(name = "preferences", columnDefinition = "jsonb")
    @JsonIgnore
    private JsonNode preferencesJson;

    @ElementCollection
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill", length = 100)
    @JsonIgnore
    private Set<String> skills = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "email_verified")
    private boolean emailVerified = false;

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private List<Project> createdProjects = new ArrayList<>();

    @OneToMany(mappedBy = "finisher", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private Set<ProjectApplication> applications = new HashSet<>();
}
