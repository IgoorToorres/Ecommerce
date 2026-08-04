package com.ecommerce.application.product.service;

import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.application.product.repository.ProductRepository;
import com.ecommerce.domain.product.Product;

import java.util.UUID;

public class DeleteProductService {
    private final ProductRepository productRepository;

    public DeleteProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public void delete(UUID id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        product.deactivate();

        productRepository.save(product);
    }
}
