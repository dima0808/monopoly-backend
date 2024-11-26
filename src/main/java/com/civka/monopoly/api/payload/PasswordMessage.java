package com.civka.monopoly.api.payload;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PasswordMessage {

    private String password;
}
