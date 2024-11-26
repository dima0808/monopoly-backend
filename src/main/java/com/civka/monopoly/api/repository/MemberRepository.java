package com.civka.monopoly.api.repository;

import com.civka.monopoly.api.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
