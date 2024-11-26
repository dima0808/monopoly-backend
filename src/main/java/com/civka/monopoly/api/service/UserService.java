package com.civka.monopoly.api.service;

import com.civka.monopoly.api.dto.UserDto;
import com.civka.monopoly.api.entity.User;

import java.util.List;

public interface UserService {

    User update(User user);

    User findByUsername(String username);

    List<User> findUsersByNicknameContaining(String nickname);

    List<User> findAll();

    User findByNickname(String nickname);

    User findById(Long id);

    User findByUsernameOrEmail(String usernameOrEmail);

    User update(User user, UserDto userDto);

    User updateFields(User user, UserDto userDto);

    User updateFieldsAdmin(User user, UserDto userDto);

    void deleteById(Long id);
}
