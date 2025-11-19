package com.ecommerce.dto;

import java.io.Serializable;

public record CartItemViewDto(ProductViewDto product) implements Serializable {
}
