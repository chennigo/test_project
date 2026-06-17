package com.systelm.dto;

import java.util.List;

public record LoginResponse(
    String token,
    String username,
    String displayName,
    String role,
    List<Long> warehouseIds
) {}
