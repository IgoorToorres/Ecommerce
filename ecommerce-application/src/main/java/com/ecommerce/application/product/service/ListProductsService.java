package com.ecommerce.application.product.service;

import com.ecommerce.application.product.repository.ProductRepository;
import com.ecommerce.application.product.response.ProductResponse;
import com.ecommerce.domain.product.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ListProductsService {
    private final ProductRepository productRepository;

    public ListProductsService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public List<ProductResponse> findAll(){
        List<Product> products = productRepository.findAll();
        List<ProductResponse> responses = new ArrayList<>();

        for(Product product : products){
           ProductResponse response = new ProductResponse(
                   product.getId(),
                   product.getName(),
                   product.getDescription(),
                   product.getPrice(),
                   product.getStockQuantity(),
                   product.isActive(),
                   product.getCreatedAt(),
                   product.getUpdatedAt()
           );
           responses.add(response);
        }

        return responses;

    }
}
