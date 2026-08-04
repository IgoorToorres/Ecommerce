package com.ecommerce.api.product;

import com.ecommerce.api.product.request.CreateProductRequest;
import com.ecommerce.api.product.request.UpdateProductRequest;
import com.ecommerce.application.product.command.CreateProductCommand;
import com.ecommerce.application.product.command.UpdateProductCommand;
import com.ecommerce.application.product.response.ProductResponse;
import com.ecommerce.application.product.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final CreateProductService createProductService;
    private final GetProductByIdService getProductByIdService;
    private final ListProductsService listProductsService;
    private final UpdateProductService updateProductService;
    private final DeleteProductService deleteProductService;

    public ProductController(
            CreateProductService createProductService,
            GetProductByIdService getProductByIdService,
            ListProductsService listProductsService,
            UpdateProductService updateProductService,
            DeleteProductService deleteProductService
    ){
        this.createProductService = createProductService;
        this.getProductByIdService = getProductByIdService;
        this.listProductsService = listProductsService;
        this.updateProductService = updateProductService;
        this.deleteProductService = deleteProductService;

    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request){
        CreateProductCommand command = new CreateProductCommand(
                request.name(),
                request.description(),
                request.price(),
                request.stockQuantity()
        );

        ProductResponse response = createProductService.create(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable UUID id){
        ProductResponse response = getProductByIdService.findById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll(){
        List<ProductResponse> response = listProductsService.findAll();

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

        ProductResponse response = updateProductService.update(command);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        deleteProductService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
