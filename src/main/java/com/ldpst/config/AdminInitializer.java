package com.ldpst.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.ldpst.service.UserService;

@Component
public class AdminInitializer implements ApplicationRunner {
    private final UserService userService;
    private final String username;
    private final String password;

    public AdminInitializer(UserService userService,
            @Value("${app.admin.username:}") String username,
            @Value("${app.admin.password:}") String password) {
        this.userService = userService;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        userService.createAdminIfMissing(username, password);
    }
}
