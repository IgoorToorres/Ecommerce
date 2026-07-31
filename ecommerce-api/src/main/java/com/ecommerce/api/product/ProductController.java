package com.ecommerce.api.product;

import com.ecommerce.application.product.CreateProductCommand;
import com.ecommerce.application.product.CreateProductHandler;
import com.ecommerce.application.product.GetProductByIdHandler;
import com.ecommerce.application.product.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final CreateProductHandler createProductHandler;
    private final GetProductByIdHandler getProductByIdHandler;

    public ProductController(CreateProductHandler createProductHandler, GetProductByIdHandler getProductByIdHandler){
        this.createProductHandler = createProductHandler;
        this.getProductByIdHandler = getProductByIdHandler;
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

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable UUID id){
        ProductResponse response = getProductByIdHandler.handle(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
