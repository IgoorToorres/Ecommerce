package com.ecommerce.api.product;

import com.ecommerce.application.product.CreateProductCommand;
import com.ecommerce.application.product.CreateProductHandler;
import com.ecommerce.application.product.GetProductByIdHandler;
import com.ecommerce.application.product.ListProductsHandler;
import com.ecommerce.application.product.ProductResponse;
import com.ecommerce.application.product.UpdateProductCommand;
import com.ecommerce.application.product.UpdateProductHandler;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final CreateProductHandler createProductHandler;
    private final GetProductByIdHandler getProductByIdHandler;
    private final ListProductsHandler listProductsHandler;
    private final UpdateProductHandler updateProductHandler;

    public ProductController(
            CreateProductHandler createProductHandler,
            GetProductByIdHandler getProductByIdHandler,
            ListProductsHandler listProductsHandler,
            UpdateProductHandler updateProductHandler
    ){
        this.createProductHandler = createProductHandler;
        this.getProductByIdHandler = getProductByIdHandler;
        this.listProductsHandler = listProductsHandler;
        this.updateProductHandler = updateProductHandler;
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

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll(){
        List<ProductResponse> response = listProductsHandler.handle();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@Valid @RequestBody UpdateProductRequest request, @PathVariable UUID id){
        UpdateProductCommand command = new UpdateProductCommand(
                id,
                request.name(),
                request.description(),
                request.price(),
                request.stockQuantity()
        );

        ProductResponse response = updateProductHandler.handle(command);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

    }
}
