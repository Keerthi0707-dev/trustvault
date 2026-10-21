package com.trustvault.controller;

import com.trustvault.dto.UserDto;
import com.trustvault.entity.User;
import com.trustvault.exception.AccessDeniedException;
import com.trustvault.security.SecurityUtils;
import com.trustvault.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam(value = "search", required = false) String search) {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));

        List<UserDto> users = userService.searchUsersForSharing(search, currentUser);
        return ResponseEntity.ok(users);
    }
}
