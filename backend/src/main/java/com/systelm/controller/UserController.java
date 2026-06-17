package com.systelm.controller;

import com.systelm.dto.CreateUserCommand;
import com.systelm.dto.UpdateUserCommand;
import com.systelm.dto.UserResponse;
import com.systelm.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('admin')")
    public List<UserResponse> listUsers() {
        return userService.listUsers();
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public UserResponse createUser(@RequestBody CreateUserCommand cmd) {
        return userService.createUser(cmd);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UpdateUserCommand cmd) {
        return userService.updateUser(id, cmd);
    }
}
