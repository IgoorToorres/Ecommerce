package com.ecommerce.api.product;

import com.ecommerce.application.product.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final CreateProductHandler createProductHandler;
    private final GetProductByIdHandler getProductByIdHandler;
    private final ListProductsHandler listProductsHandler;

    public ProductController(
            CreateProductHandler createProductHandler,
            GetProductByIdHandler getProductByIdHandler,
            ListProductsHandler listProductsHandler
    ){
        this.createProductHandler = createProductHandler;
        this.getProductByIdHandler = getProductByIdHandler;
        this.listProductsHandler = listProductsHandler;
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

    @GetMapping()
    public ResponseEntity<List<ProductResponse>> getAll(){
        List<ProductResponse> response = listProductsHandler.handle();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
