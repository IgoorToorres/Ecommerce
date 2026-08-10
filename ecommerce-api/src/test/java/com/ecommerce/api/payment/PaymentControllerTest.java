package com.ecommerce.api.payment;

import com.ecommerce.api.exception.GlobalExceptionHandler;
import com.ecommerce.api.ratelimit.RateLimitFilter;
import com.ecommerce.api.security.JwtAuthenticationFilter;
import com.ecommerce.api.security.SecurityConfig;
import com.ecommerce.application.exception.ForbiddenException;
import com.ecommerce.application.payment.command.ProcessPaymentCommand;
import com.ecommerce.application.payment.response.PaymentResponse;
import com.ecommerce.application.payment.service.ProcessPaymentService;
import com.ecommerce.application.user.security.AuthenticatedUser;
import com.ecommerce.application.user.security.TokenService;
import com.ecommerce.domain.payment.PaymentMethod;
import com.ecommerce.domain.payment.PaymentStatus;
import com.ecommerce.domain.user.UserRole;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
@Import({
        PaymentController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RateLimitFilter.class,
        GlobalExceptionHandler.class
})
@ImportAutoConfiguration({
        SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProcessPaymentService processPaymentService;

    @MockitoBean
    private TokenService tokenService;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }

    @Test
    void shouldProcessPaymentWithValidToken() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                customerId,
                "customer@email.com",
                UserRole.CUSTOMER
        );
        PaymentResponse paymentResponse = new PaymentResponse(
                paymentId,
                orderId,
                PaymentStatus.APPROVED,
                PaymentMethod.PIX,
                BigDecimal.valueOf(200),
                "payment-key-1",
                "external-123",
                Instant.parse("2026-08-10T10:00:00Z"),
                Instant.parse("2026-08-10T10:01:00Z")
        );

        when(tokenService.validateToken("customer-token"))
                .thenReturn(authenticatedUser);
        when(processPaymentService.process(any(ProcessPaymentCommand.class)))
                .thenReturn(paymentResponse);

        mockMvc.perform(post("/api/orders/{orderId}/payments", orderId)
                        .header("Authorization", "Bearer customer-token")
                        .header("Idempotency-Key", "payment-key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "method": "PIX",
                                  "approved": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId.toString()))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.method").value("PIX"))
                .andExpect(jsonPath("$.amount").value(200))
                .andExpect(jsonPath("$.idempotencyKey").value("payment-key-1"))
                .andExpect(jsonPath("$.externalReference").value("external-123"));

        ArgumentCaptor<ProcessPaymentCommand> commandCaptor =
                ArgumentCaptor.forClass(ProcessPaymentCommand.class);
        verify(processPaymentService).process(commandCaptor.capture());

        ProcessPaymentCommand command = commandCaptor.getValue();
        assertThat(command.orderId()).isEqualTo(orderId);
        assertThat(command.authenticatedUserId()).isEqualTo(customerId);
        assertThat(command.authenticatedUserRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(command.method()).isEqualTo(PaymentMethod.PIX);
        assertThat(command.idempotencyKey()).isEqualTo("payment-key-1");
        assertThat(command.approved()).isTrue();
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
        UUID orderId = UUID.randomUUID();

        mockMvc.perform(post("/api/orders/{orderId}/payments", orderId)
                        .header("Idempotency-Key", "payment-key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "method": "PIX",
                                  "approved": true
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Token de autenticação não informado ou inválido."))
                .andExpect(jsonPath("$.path").value("/api/orders/" + orderId + "/payments"));

        verifyNoInteractions(processPaymentService);
    }

    @Test
    void shouldReturnBadRequestWhenMethodIsMissing() throws Exception {
        UUID orderId = UUID.randomUUID();
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                UUID.randomUUID(),
                "customer@email.com",
                UserRole.CUSTOMER
        );

        when(tokenService.validateToken("customer-token"))
                .thenReturn(authenticatedUser);

        mockMvc.perform(post("/api/orders/{orderId}/payments", orderId)
                        .header("Authorization", "Bearer customer-token")
                        .header("Idempotency-Key", "payment-key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "approved": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("method: O metodo é obrigatorio"))
                .andExpect(jsonPath("$.path").value("/api/orders/" + orderId + "/payments"));

        verifyNoInteractions(processPaymentService);
    }

    @Test
    void shouldReturnForbiddenWhenCustomerTriesToPayAnotherCustomerOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                UUID.randomUUID(),
                "customer@email.com",
                UserRole.CUSTOMER
        );

        when(tokenService.validateToken("customer-token"))
                .thenReturn(authenticatedUser);
        when(processPaymentService.process(any(ProcessPaymentCommand.class)))
                .thenThrow(new ForbiddenException("Você não tem permissão para pagar este pedido."));

        mockMvc.perform(post("/api/orders/{orderId}/payments", orderId)
                        .header("Authorization", "Bearer customer-token")
                        .header("Idempotency-Key", "payment-key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "method": "PIX",
                                  "approved": true
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Você não tem permissão para pagar este pedido."))
                .andExpect(jsonPath("$.path").value("/api/orders/" + orderId + "/payments"));
    }
}
