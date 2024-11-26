package com.civka.monopoly.api.controller;

import com.civka.monopoly.api.dto.UserDto;
import com.civka.monopoly.api.entity.User;
import com.civka.monopoly.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PatchMapping("/users/{username}")
    public ResponseEntity<User> updateUserFields(@RequestBody UserDto userDto, @PathVariable String username) {
        User user = userService.findByUsername(username);
        return ResponseEntity.ok(userService.updateFieldsAdmin(user, userDto));
    }
}
