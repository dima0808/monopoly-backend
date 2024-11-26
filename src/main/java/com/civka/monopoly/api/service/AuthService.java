package com.civka.monopoly.api.service;

import com.civka.monopoly.api.dto.SignInDto;
import com.civka.monopoly.api.dto.SignUpDto;
import com.civka.monopoly.api.entity.User;

public interface AuthService {

    String signIn(SignInDto signInDto);

    User signUp(SignUpDto signUpDto);
}
