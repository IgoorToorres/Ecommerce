package com.ecommerce.application.product;

import com.ecommerce.domain.product.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ListProductsHandler {
    private final ProductRepository productRepository;

    public ListProductsHandler(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public List<ProductResponse> handle(){
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
