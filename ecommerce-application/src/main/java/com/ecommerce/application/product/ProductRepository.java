package com.ecommerce.application.product;

import com.ecommerce.domain.product.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID id);
}
