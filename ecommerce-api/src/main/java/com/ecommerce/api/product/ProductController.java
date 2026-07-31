package com.ecommerce.api.product;

import com.ecommerce.application.product.CreateProductCommand;
import com.ecommerce.application.product.CreateProductHandler;
import com.ecommerce.application.product.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final CreateProductHandler createProductHandler;

    public ProductController(CreateProductHandler createProductHandler){
        this.createProductHandler = createProductHandler;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request){
        CreateProductCommand command = new CreateProductCommand(
                request.name(),
                request.description(),
                request.price(),
                request.stockQuantity()
        );

        ProductResponse response = createProductHandler.handle(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
