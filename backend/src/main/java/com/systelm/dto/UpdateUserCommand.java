package com.systelm.dto;

import java.util.List;

public record UpdateUserCommand(
    String displayName,
    String roleName,
    List<Long> warehouseIds,
    String status,
    String password
) {}
