package com.ecommerce.dto;

import java.math.BigDecimal;

public interface ProductAdminView {
  Long getId();

  String getName();

  String getDescription();

  BigDecimal getPrice();

  int getStockQuantity();

  String getCategoriesString();

  boolean getIsDeleted();
}
