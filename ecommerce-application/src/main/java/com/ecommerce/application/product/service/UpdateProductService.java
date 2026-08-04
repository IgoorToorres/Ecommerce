package com.ecommerce.application.product.service;

import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.application.product.command.UpdateProductCommand;
import com.ecommerce.application.product.repository.ProductRepository;
import com.ecommerce.application.product.response.ProductResponse;
import com.ecommerce.domain.product.Product;
import org.springframework.stereotype.Service;

@Service
public class UpdateProductService {
    private final ProductRepository productRepository;

    public UpdateProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public ProductResponse update(UpdateProductCommand command){
        Product product = productRepository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));

        product.updateDetails(
                command.name(),
                command.description(),
                command.price(),
                command.stockQuantity()
        );

        Product productSaved = productRepository.save(product);

        return new ProductResponse(
                productSaved.getId(),
                productSaved.getName(),
                productSaved.getDescription(),
                productSaved.getPrice(),
                productSaved.getStockQuantity(),
                productSaved.isActive(),
                productSaved.getCreatedAt(),
                productSaved.getUpdatedAt()
        );
    }
}
