package com.civka.monopoly.api.repository;

import com.civka.monopoly.api.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findByName(String name);

    Boolean existsByName(String name);

    @Query("SELECT c FROM Chat c WHERE c.name LIKE %?1% AND (c.name LIKE CONCAT(?1, ' %') OR c.name LIKE CONCAT('% ', ?1, ' %') OR c.name LIKE CONCAT('% ', ?1))")
    List<Chat> findAllByUsername(String username);

    void deleteByName(String chatName);
}
