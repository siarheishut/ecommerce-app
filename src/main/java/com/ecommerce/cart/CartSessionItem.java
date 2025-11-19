package com.ecommerce.cart;

import com.ecommerce.dto.ProductViewDto;

import java.io.Serializable;

public record CartSessionItem(
    ProductViewDto product,
    int quantity
) implements Serializable {
}
