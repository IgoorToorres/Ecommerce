package com.ecommerce.infrastructure.persistence.product;

import com.ecommerce.application.product.ProductRepository;
import com.ecommerce.domain.product.Product;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    public ProductRepositoryAdapter(ProductJpaRepository productJpaRepository){
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

}
