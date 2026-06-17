package com.systelm.dto;

public record CreateCategoryCommand(
    String name,
    Long parentId
) {}
