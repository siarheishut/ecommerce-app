package com.ecommerce.dto;

import java.time.Instant;

public record ReviewDto(
    String authorUsername,
    int rating,
    String comment,
    Instant createdAt
) {
}
