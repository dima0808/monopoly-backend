package com.civka.monopoly.api.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class JwtResponse {

    private String token;
    private String username;
    private String nickname;
    private String role;
}
