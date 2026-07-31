package com.ecommerce.application.product;

import com.ecommerce.domain.product.Product;

public interface ProductRepository {
    Product save(Product product);
}
