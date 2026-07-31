package com.ecommerce.application.product;


import com.ecommerce.domain.product.Product;
import org.springframework.stereotype.Service;

@Service
public final class CreateProductHandler {
    private final ProductRepository productRepository;

    public CreateProductHandler(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public ProductResponse handle(CreateProductCommand command){
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
