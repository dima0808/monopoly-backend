package com.civka.monopoly.api.service.impl;

import com.civka.monopoly.api.dto.SignInDto;
import com.civka.monopoly.api.dto.SignUpDto;
import com.civka.monopoly.api.entity.Role;
import com.civka.monopoly.api.entity.User;
import com.civka.monopoly.api.repository.RoleRepository;
import com.civka.monopoly.api.repository.UserRepository;
import com.civka.monopoly.api.security.jwt.JwtUtils;
import com.civka.monopoly.api.service.AuthService;
import com.civka.monopoly.api.service.RoleNotFoundException;
import com.civka.monopoly.api.service.UserAlreadyExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public String signIn(SignInDto signInDto) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                signInDto.getLogin(), signInDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return jwtUtils.generateJwtToken(authentication);
    }

    @Override
    public User signUp(SignUpDto signUpDto) {

        if (userRepository.existsByUsername(signUpDto.getUsername())) {
            throw new UserAlreadyExistException("User with username " + signUpDto.getUsername() + " already exists");
        }
        if (userRepository.existsByEmail(signUpDto.getEmail())) {
            throw new UserAlreadyExistException("User with email " + signUpDto.getEmail() + " already exists");
        }

        User user = User.builder()
                .username(signUpDto.getUsername())
                .nickname(signUpDto.getUsername())
                .email(signUpDto.getEmail())
                .elo(0)
                .matchesPlayed(0)
                .matchesWon(0)
                .averagePlacement(0.0f)
                .password(passwordEncoder.encode(signUpDto.getPassword())).build();

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("ROLE_USER"));
        Set<Role> roles = Set.of(userRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }
}
