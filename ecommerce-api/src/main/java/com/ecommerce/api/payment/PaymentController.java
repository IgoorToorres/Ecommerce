package com.ecommerce.api.payment;

import com.ecommerce.api.exception.ApiErrorResponse;
import com.ecommerce.api.payment.request.ProcessPaymentRequest;
import com.ecommerce.application.payment.command.ProcessPaymentCommand;
import com.ecommerce.application.payment.response.PaymentResponse;
import com.ecommerce.application.payment.service.ProcessPaymentService;
import com.ecommerce.application.user.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders/{orderId}/payments")
@Tag(name = "Payments", description = "Operações para processamento de pagamentos")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final ProcessPaymentService processPaymentService;

    public PaymentController(
            ProcessPaymentService processPaymentService
    ){
        this.processPaymentService = processPaymentService;
    }

    @PostMapping
    @Operation(
            summary = "Processar pagamento",
            description = "Processa um pagamento simulado para um pedido usando uma chave de idempotência."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pagamento processado com sucesso ou resposta idempotente retornada",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token de autenticação não informado ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário sem permissão para pagar este pedido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<PaymentResponse> process(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Parameter(description = "Identificador do pedido que será pago")
            @PathVariable UUID orderId,
            @Parameter(description = "Chave usada para evitar processamento duplicado do mesmo pagamento")
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ProcessPaymentRequest request
            ){

        ProcessPaymentCommand command = new ProcessPaymentCommand(
                orderId,
                authenticatedUser.id(),
                authenticatedUser.role(),
                request.method(),
                idempotencyKey,
                request.approved()
        );

        PaymentResponse response = processPaymentService.process(command);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
