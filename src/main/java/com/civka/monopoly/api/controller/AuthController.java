package com.civka.monopoly.api.controller;

import com.civka.monopoly.api.dto.SignInDto;
import com.civka.monopoly.api.dto.SignUpDto;
import com.civka.monopoly.api.entity.User;
import com.civka.monopoly.api.payload.JwtResponse;
import com.civka.monopoly.api.service.AuthService;
import com.civka.monopoly.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody SignInDto signInDto) {
        String jwt = authService.signIn(signInDto);
        User user = userService.findByUsernameOrEmail(signInDto.getLogin());
        String role = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN")) ? "ROLE_ADMIN" : "ROLE_USER";
        return ResponseEntity.ok(JwtResponse.builder()
                .token(jwt)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(role)
                .build());
    }

    @PostMapping("/signup")
    public ResponseEntity<User> getRegisterPage(@RequestBody SignUpDto signUpDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(signUpDto));
    }
}
