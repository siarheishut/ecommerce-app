package com.ecommerce.dto;

import jakarta.validation.constraints.*;

public record ReviewSubmissionDto(
    @NotNull(message = "Rating cannot be empty.")
    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating must be at most 5.")
    Integer rating,

    @Size(max = 1000, message = "Comment cannot exceed 1000 characters.")
    String comment
) {
}
