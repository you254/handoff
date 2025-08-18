package com.handoff.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.handoff.model.entity.User;
import com.handoff.model.enums.UserRole;
import com.handoff.model.enums.UserStatus;
import com.handoff.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserLookupService userLookupService;

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User register(String email, String rawPassword, String firstName, String lastName, UserRole role) {
        User u = new User();
        u.setEmail(email.toLowerCase());
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setRole(role);
        return userRepository.save(u);
    }

    public User findByEmailOrThrow(String email) {
        return userLookupService.findByEmailOrThrow(email);
    }

    public User findByIdOrThrow(UUID id) {
        return userLookupService.findByIdOrThrow(id);
    }


    @Transactional
    public User updateProfile(String email,
                              String firstName,
                              String lastName,
                              JsonNode profileJson,
                              JsonNode preferencesJson,
                              Set<String> skills) {
        User u = userLookupService.findByEmailOrThrow(email);
        if (firstName != null) u.setFirstName(firstName);
        if (lastName != null) u.setLastName(lastName);
        if (profileJson != null) u.setProfileJson(profileJson);
        if (preferencesJson != null) u.setPreferencesJson(preferencesJson);
        if (skills != null) u.setSkills(skills);
        return userRepository.save(u);
    }

    @Transactional
    public void deactivate(String email) {
        User u = userLookupService.findByEmailOrThrow(email);
        u.setStatus(UserStatus.INACTIVE);
        userRepository.save(u);
    }

    @Transactional
    public void updateLastLoginAt(UUID id) {
        User user = findByIdOrThrow(id);
        user.setLastLoginAt(java.time.Instant.now());
        userRepository.save(user);
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userLookupService.findByEmailOrThrow(username);
        Collection<? extends GrantedAuthority> authorities = rolesToAuthorities(user.getRole());
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                authorities
        );
    }

    private List<GrantedAuthority> rolesToAuthorities(UserRole role) {
        return switch (role) {
            case CREATOR -> List.of(new SimpleGrantedAuthority("ROLE_CREATOR"));
            case FINISHER -> List.of(new SimpleGrantedAuthority("ROLE_FINISHER"));
            case BOTH -> List.of(
                    new SimpleGrantedAuthority("ROLE_CREATOR"),
                    new SimpleGrantedAuthority("ROLE_FINISHER"));
        };
    }
}
