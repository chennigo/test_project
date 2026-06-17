package com.systelm.service;

import com.systelm.dto.CreateUserCommand;
import com.systelm.dto.UpdateUserCommand;
import com.systelm.dto.UserResponse;
import com.systelm.entity.Role;
import com.systelm.entity.UserAccount;
import com.systelm.entity.Warehouse;
import com.systelm.repository.RoleRepository;
import com.systelm.repository.UserAccountRepository;
import com.systelm.repository.WarehouseRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final WarehouseRepository warehouseRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserAccountRepository userAccountRepository,
            RoleRepository roleRepository,
            WarehouseRepository warehouseRepository,
            PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.warehouseRepository = warehouseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> listUsers() {
        return userAccountRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserCommand cmd) {
        if (userAccountRepository.findByUsername(cmd.username()).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }
        Role role = resolveRole(cmd.roleName());
        Set<Warehouse> warehouses = resolveWarehouses(cmd.warehouseIds());

        UserAccount user = new UserAccount();
        user.setUsername(cmd.username());
        user.setPasswordHash(passwordEncoder.encode(cmd.password()));
        user.setDisplayName(cmd.displayName());
        user.setRole(role);
        user.setWarehouses(warehouses);
        return toResponse(userAccountRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserCommand cmd) {
        UserAccount user = userAccountRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (cmd.displayName() != null) {
            user.setDisplayName(cmd.displayName());
        }
        if (cmd.roleName() != null) {
            user.setRole(resolveRole(cmd.roleName()));
        }
        if (cmd.warehouseIds() != null) {
            user.setWarehouses(resolveWarehouses(cmd.warehouseIds()));
        }
        if (cmd.status() != null) {
            user.setStatus(cmd.status());
        }
        if (cmd.password() != null && !cmd.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(cmd.password()));
        }
        return toResponse(userAccountRepository.save(user));
    }

    private Role resolveRole(String roleName) {
        return roleRepository.findByName(roleName)
            .orElseThrow(() -> new IllegalArgumentException("角色不存在"));
    }

    private Set<Warehouse> resolveWarehouses(List<Long> warehouseIds) {
        if (warehouseIds == null || warehouseIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Warehouse> warehouses = warehouseRepository.findAllById(warehouseIds);
        if (warehouses.size() != warehouseIds.size()) {
            throw new IllegalArgumentException("仓库不存在");
        }
        return new HashSet<>(warehouses);
    }

    private UserResponse toResponse(UserAccount user) {
        List<Long> warehouseIds = user.getWarehouses().stream()
            .map(Warehouse::getId)
            .toList();
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getDisplayName(),
            user.getRole().getName(),
            warehouseIds,
            user.getStatus()
        );
    }
}
