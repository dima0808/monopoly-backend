package com.civka.monopoly.api.config;

import com.civka.monopoly.api.entity.Chat;
import com.civka.monopoly.api.entity.Role;
import com.civka.monopoly.api.entity.User;
import com.civka.monopoly.api.repository.ChatRepository;
import com.civka.monopoly.api.repository.RoleRepository;
import com.civka.monopoly.api.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class SetupDataLoader implements
        ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ChatRepository chatRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {

        Role userRole = createRoleIfNotFound("ROLE_USER");
        Role adminRole = createRoleIfNotFound("ROLE_ADMIN");

        if (!chatRepository.existsByName("public")) {
            Chat chat = Chat.builder()
                    .name("public")
                    .isLobbyChat(false)
                    .build();
            chatRepository.save(chat);
        }

        if (!userRepository.existsByUsernameOrEmail("dimitri", "mamchenko2210@gmail.com")) {
            User user = User.builder()
                    .username("dimitri")
                    .nickname("dimitri")
                    .email("mamchenko2210@gmail.com")
                    .password(passwordEncoder.encode("gk7dlA9grTjpIP12"))
                    .roles(Set.of(userRole, adminRole))
                    .build();
            userRepository.save(user);
        }

        if (!userRepository.existsByUsernameOrEmail("nazar", "pupikpn@gmail.com")) {
            User user = User.builder()
                    .username("nazar")
                    .nickname("nazar")
                    .email("pupikpn@gmail.com")
                    .password(passwordEncoder.encode("trh1fjf7dog228"))
                    .roles(Set.of(userRole, adminRole))
                    .build();
            userRepository.save(user);
        }
    }

    @Transactional
    Role createRoleIfNotFound(String name) {

        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = Role.builder().name(name).build();
            roleRepository.save(role);
            return role;
        });
    }
}
