package com.ecommerce.application.product.service;

import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.application.product.repository.ProductRepository;
import com.ecommerce.application.product.response.ProductResponse;
import com.ecommerce.domain.product.Product;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetProductByIdService {
    private final ProductRepository productRepository;

    public GetProductByIdService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public ProductResponse findById(UUID id){
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
