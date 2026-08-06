package com.ecommerce.api.order;

import com.ecommerce.api.exception.ApiErrorResponse;
import com.ecommerce.api.order.request.CreateOrderItemRequest;
import com.ecommerce.api.order.request.CreateOrderRequest;
import com.ecommerce.application.order.command.CreateOrderCommand;
import com.ecommerce.application.order.command.CreateOrderItemCommand;
import com.ecommerce.application.order.command.ListOrdersCommand;
import com.ecommerce.application.order.response.OrderResponse;
import com.ecommerce.application.order.response.PageResponse;
import com.ecommerce.application.order.service.CreateOrderService;
import com.ecommerce.application.order.service.GetOrderByIdService;
import com.ecommerce.application.order.service.ListOrdersService;
import com.ecommerce.application.user.security.AuthenticatedUser;
import com.ecommerce.domain.order.OrderStatus;
import com.ecommerce.domain.user.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Operações para gerenciamento de pedidos")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final CreateOrderService createOrderService;
    private final GetOrderByIdService getOrderByIdService;
    private final ListOrdersService listOrdersService;

    public OrderController(
            CreateOrderService createOrderService,
            GetOrderByIdService getOrderByIdService,
            ListOrdersService listOrdersService
    ) {
        this.createOrderService = createOrderService;
        this.getOrderByIdService = getOrderByIdService;
        this.listOrdersService = listOrdersService;
    }

    @PostMapping
    @Operation(
            summary = "Criar pedido",
            description = "Cria um novo pedido, valida produtos ativos, verifica estoque e realiza a baixa das quantidades solicitadas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido criado com sucesso",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Regra de negócio violada, como produto inativo ou estoque insuficiente",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token de autenticação não informado ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<OrderResponse> create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateOrderRequest request
    ){
        List<CreateOrderItemCommand> items = new ArrayList<>();

        for(CreateOrderItemRequest item : request.items()){
            items.add(new CreateOrderItemCommand(
                    item.productId(),
                    item.quantity()
            ));
        }

        CreateOrderCommand command = new CreateOrderCommand(
                authenticatedUser.id(),
                items
        );

        OrderResponse response = createOrderService.create(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar pedido por ID",
            description = "Retorna os dados de um pedido. Usuários CUSTOMER só podem acessar os próprios pedidos."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido encontrado",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token de autenticação não informado ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário sem permissão para acessar este pedido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<OrderResponse> findById(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID id
    ) {
        OrderResponse response = getOrderByIdService.getById(id);

        if (authenticatedUser.role() == UserRole.CUSTOMER
                && !response.customerId().equals(authenticatedUser.id())) {
            throw new AccessDeniedException("Você não tem permissão para acessar este pedido.");
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping
    @Operation(
            summary = "Listar pedidos",
            description = "Lista pedidos com paginação e filtros opcionais por cliente e status."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedidos listados com sucesso",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token de autenticação não informado ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<PageResponse<OrderResponse>> findAll(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID customerIdFilter = customerId;

        if (authenticatedUser.role() == UserRole.CUSTOMER) {
            customerIdFilter = authenticatedUser.id();
        }

        ListOrdersCommand command = new ListOrdersCommand(
                customerIdFilter,
                status,
                page,
                size
        );

        PageResponse<OrderResponse> response = listOrdersService.findAll(command);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

}
