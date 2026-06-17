package com.systelm.dto;

import java.util.List;

public record UserResponse(
    Long id,
    String username,
    String displayName,
    String roleName,
    List<Long> warehouseIds,
    String status
) {}
