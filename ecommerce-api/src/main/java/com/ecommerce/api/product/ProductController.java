package com.ecommerce.api.product;

import com.ecommerce.api.product.request.CreateProductRequest;
import com.ecommerce.api.product.request.UpdateProductRequest;
import com.ecommerce.api.exception.ApiErrorResponse;
import com.ecommerce.application.product.command.CreateProductCommand;
import com.ecommerce.application.product.command.UpdateProductCommand;
import com.ecommerce.application.product.response.ProductResponse;
import com.ecommerce.application.product.service.CreateProductService;
import com.ecommerce.application.product.service.DeleteProductService;
import com.ecommerce.application.product.service.GetProductByIdService;
import com.ecommerce.application.product.service.ListProductsService;
import com.ecommerce.application.product.service.UpdateProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@Tag(name = "Products", description = "Operacoes para gerenciamento de produtos")
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
    @Operation(summary = "Criar produto", description = "Cadastra um novo produto ativo no catalogo.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Produto criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
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
    @Operation(summary = "Buscar produto por ID", description = "Retorna os dados de um produto cadastrado.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Produto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto nao encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<ProductResponse> findById(
            @Parameter(description = "ID do produto", required = true)
            @PathVariable UUID id
    ){
        ProductResponse response = getProductByIdService.findById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping
    @Operation(summary = "Listar produtos", description = "Retorna todos os produtos cadastrados.")
    @ApiResponse(
            responseCode = "200",
            description = "Produtos listados com sucesso",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class)))
    )
    public ResponseEntity<List<ProductResponse>> getAll(){
        List<ProductResponse> response = listProductsService.findAll();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto", description = "Atualiza os dados cadastrais de um produto existente.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Produto atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto nao encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<ProductResponse> update(
            @Valid @RequestBody UpdateProductRequest request,
            @Parameter(description = "ID do produto", required = true)
            @PathVariable UUID id
    ){
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
    @Operation(summary = "Desativar produto", description = "Realiza delete logico, marcando o produto como inativo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produto desativado com sucesso"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto nao encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do produto", required = true)
            @PathVariable UUID id
    ){
        deleteProductService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
