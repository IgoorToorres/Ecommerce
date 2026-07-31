package com.ecommerce.application.product;

import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.domain.product.Product;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetProductByIdHandler {
    private final ProductRepository productRepository;

    public GetProductByIdHandler(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public ProductResponse handle(UUID id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));

        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStockQuantity(),
            product.isActive(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}
