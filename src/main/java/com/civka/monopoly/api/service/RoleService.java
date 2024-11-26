package com.civka.monopoly.api.service;

import com.civka.monopoly.api.entity.Role;

public interface RoleService {

    Role findByName(String name);
}
