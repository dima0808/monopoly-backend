package com.civka.monopoly.api.repository;

import com.civka.monopoly.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findByNicknameContaining(String nickname);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Boolean existsByUsernameOrEmail(String admin, String mail);

    Boolean existsByNickname(String nickname);
}
