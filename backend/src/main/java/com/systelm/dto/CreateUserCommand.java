package com.systelm.dto;

import java.util.List;

public record CreateUserCommand(
    String username,
    String password,
    String displayName,
    String roleName,
    List<Long> warehouseIds
) {}
