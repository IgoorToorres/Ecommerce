package com.ecommerce.application.product.service;

import com.ecommerce.application.product.command.CreateProductCommand;
import com.ecommerce.application.product.repository.ProductRepository;
import com.ecommerce.application.product.response.ProductResponse;
import com.ecommerce.domain.product.Product;
import org.springframework.stereotype.Service;

@Service
public final class CreateProductService {
    private final ProductRepository productRepository;

    public CreateProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public ProductResponse create(CreateProductCommand command){
        Product product = new Product(
                command.name(),
                command.description(),
                command.price(),
                command.stockQuantity()
        );

        Product savedProduct = productRepository.save(product);

        return new ProductResponse(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice(),
                savedProduct.getStockQuantity(),
                savedProduct.isActive(),
                savedProduct.getCreatedAt(),
                savedProduct.getUpdatedAt()
        );
    }
}
