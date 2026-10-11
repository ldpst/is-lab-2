package com.ldpst.service;

import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ldpst.model.domain.AppUser;
import com.ldpst.model.domain.UserRole;
import com.ldpst.repository.AppUserRepository;
import com.ldpst.service.util.BusinessRuleException;

@Service
public class UserService implements UserDetailsService {
    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        AppUser user = repository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }

    @Transactional
    public AppUser register(String username, String password) {
        return create(username, password, UserRole.USER, false);
    }

    @Transactional
    public void createAdminIfMissing(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return;
        }
        create(username, password, UserRole.ADMIN, true);
    }

    @Transactional(readOnly = true)
    public AppUser find(String username) {
        return repository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    private AppUser create(String username, String password, UserRole role, boolean ignoreExisting) {
        String normalizedUsername = normalizeUsername(username);
        validatePassword(password);

        if (repository.existsByUsernameIgnoreCase(normalizedUsername)) {
            if (ignoreExisting) {
                return repository.findByUsernameIgnoreCase(normalizedUsername).orElseThrow();
            }
            throw new BusinessRuleException("Пользователь с таким именем уже существует");
        }

        AppUser user = new AppUser();
        user.setUsername(normalizedUsername);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        return repository.save(user);
    }

    private String normalizeUsername(String username) {
        String value = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        if (!value.matches("[a-z0-9._-]{3,50}")) {
            throw new BusinessRuleException(
                    "Имя пользователя должно содержать 3–50 латинских букв, цифр или символов . _ -");
        }
        return value;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 100) {
            throw new BusinessRuleException("Пароль должен содержать от 8 до 100 символов");
        }
    }
}
