package com.civka.monopoly.api.service.impl;

import com.civka.monopoly.api.dto.UserDto;
import com.civka.monopoly.api.entity.User;
import com.civka.monopoly.api.repository.UserRepository;
import com.civka.monopoly.api.service.UserAlreadyExistException;
import com.civka.monopoly.api.service.UserNotFoundException;
import com.civka.monopoly.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    @Override
    public List<User> findUsersByNicknameContaining(String nickname) {
        return userRepository.findByNicknameContaining(nickname);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UserNotFoundException(nickname));
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public User findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UserNotFoundException(usernameOrEmail));
    }

    @Override
    public User update(User user, UserDto userDto) {
        if (userDto.getEmail() != null && userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistException("User with email " + userDto.getEmail() + " already exists");
        }
        if (userDto.getNickname() != null && userRepository.existsByNickname(userDto.getNickname())) {
            throw new UserAlreadyExistException("User with nickname " + userDto.getNickname() + " already exists");
        }
        user.setEmail(userDto.getEmail());
        user.setNickname(userDto.getNickname());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User updateFields(User user, UserDto userDto) {
        if (userDto.getEmail() != null && userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistException("User with email " + userDto.getEmail() + " already exists");
        }
        if (userDto.getNickname() != null && userRepository.existsByNickname(userDto.getNickname())) {
            throw new UserAlreadyExistException("User with nickname " + userDto.getNickname() + " already exists");
        }
        if (userDto.getEmail() != null) user.setEmail(userDto.getEmail());
        if (userDto.getNickname() != null) user.setNickname(userDto.getNickname());
        if (userDto.getPassword() != null) user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User updateFieldsAdmin(User user, UserDto userDto) {
        User updatedUser = updateFields(user, userDto);
        if (userDto.getElo() != null) updatedUser.setElo(userDto.getElo());
        if (userDto.getMatchesPlayed() != null) updatedUser.setMatchesPlayed(userDto.getMatchesPlayed());
        if (userDto.getMatchesWon() != null) updatedUser.setMatchesWon(userDto.getMatchesWon());
        if (userDto.getAveragePlacement() != null) updatedUser.setAveragePlacement(userDto.getAveragePlacement());
        return userRepository.save(updatedUser);
    }

    @Override
    public void deleteById(Long id) {
        findById(id);
        userRepository.deleteById(id);
    }
}
