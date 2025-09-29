package com.ecommerce.cart;

import com.ecommerce.entity.Product;

public record CartSessionItem(
    Product product,
    int quantity
) {
}
