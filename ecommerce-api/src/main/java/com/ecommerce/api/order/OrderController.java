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
import com.ecommerce.domain.order.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Operações para gerenciamento de pedidos")
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
            )
    })
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request){
        List<CreateOrderItemCommand> items = new ArrayList<>();

        for(CreateOrderItemRequest item : request.items()){
            items.add(new CreateOrderItemCommand(
                    item.productId(),
                    item.quantity()
            ));
        }

        CreateOrderCommand command = new CreateOrderCommand(
                request.customerId(),
                items
        );

        OrderResponse response = createOrderService.create(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
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
            )
    })
    public ResponseEntity<OrderResponse> findById(@PathVariable UUID id){
        OrderResponse response = getOrderByIdService.getById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping
    @Operation(
            summary = "Listar pedidos",
            description = "Lista pedidos com paginação e filtros opcionais por cliente e status."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Pedidos listados com sucesso"
    )
    public ResponseEntity<PageResponse<OrderResponse>> findAll(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false)OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
            ){
        ListOrdersCommand command = new ListOrdersCommand(
                customerId,
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
