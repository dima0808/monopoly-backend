package com.civka.monopoly.api.service.impl;

import com.civka.monopoly.api.entity.Role;
import com.civka.monopoly.api.repository.RoleRepository;
import com.civka.monopoly.api.service.RoleNotFoundException;
import com.civka.monopoly.api.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RoleNotFoundException(name));
    }
}
