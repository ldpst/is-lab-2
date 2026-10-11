package com.ldpst.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ldpst.model.domain.AppUser;
import com.ldpst.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token) {
        return new CsrfResponse(token.getHeaderName(), token.getToken());
    }

    @GetMapping("/me")
    public UserResponse currentUser(Authentication authentication) {
        AppUser user = userService.find(authentication.getName());
        return new UserResponse(user.getUsername(), user.getRole().name());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        AppUser user = userService.register(request.username(), request.password());
        return new UserResponse(user.getUsername(), user.getRole().name());
    }

    public record RegisterRequest(@NotBlank @Size(max = 50) String username,
                                  @NotBlank @Size(min = 8, max = 100) String password) {
    }

    public record UserResponse(String username, String role) {
    }

    public record CsrfResponse(String headerName, String token) {
    }
}
