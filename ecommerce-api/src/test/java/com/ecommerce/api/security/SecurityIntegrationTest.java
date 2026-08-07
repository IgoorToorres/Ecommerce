package com.ecommerce.api.security;

import com.ecommerce.api.auth.AuthController;
import com.ecommerce.api.exception.GlobalExceptionHandler;
import com.ecommerce.api.order.OrderController;
import com.ecommerce.api.product.ProductController;
import com.ecommerce.api.ratelimit.RateLimitFilter;
import com.ecommerce.application.order.response.OrderResponse;
import com.ecommerce.application.order.response.PageResponse;
import com.ecommerce.application.order.service.CreateOrderService;
import com.ecommerce.application.order.service.GetOrderByIdService;
import com.ecommerce.application.order.service.ListOrdersService;
import com.ecommerce.application.product.response.ProductResponse;
import com.ecommerce.application.product.service.CreateProductService;
import com.ecommerce.application.product.service.DeleteProductService;
import com.ecommerce.application.product.service.GetProductByIdService;
import com.ecommerce.application.product.service.ListProductsService;
import com.ecommerce.application.product.service.UpdateProductService;
import com.ecommerce.application.user.response.AuthResponse;
import com.ecommerce.application.user.security.AuthenticatedUser;
import com.ecommerce.application.user.security.TokenService;
import com.ecommerce.application.user.service.LoginService;
import com.ecommerce.application.user.service.RegisterUserService;
import com.ecommerce.domain.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AuthController.class,
        OrderController.class,
        ProductController.class
})
@Import({
        AuthController.class,
        OrderController.class,
        ProductController.class,
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
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private RegisterUserService registerUserService;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private CreateOrderService createOrderService;

    @MockitoBean
    private GetOrderByIdService getOrderByIdService;

    @MockitoBean
    private ListOrdersService listOrdersService;

    @MockitoBean
    private CreateProductService createProductService;

    @MockitoBean
    private GetProductByIdService getProductByIdService;

    @MockitoBean
    private ListProductsService listProductsService;

    @MockitoBean
    private UpdateProductService updateProductService;

    @MockitoBean
    private DeleteProductService deleteProductService;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }

    @Test
    void shouldAllowLoginWithoutToken() throws Exception {
        when(loginService.login(any()))
                .thenReturn(new AuthResponse("access-token", "Bearer", 3600L));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "customer@email.com",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void shouldReturnUnauthorizedWhenAccessingOrdersWithoutToken() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Token de autenticação não informado ou inválido."))
                .andExpect(jsonPath("$.path").value("/api/orders"));
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        when(tokenService.validateToken(anyString()))
                .thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Token inválido ou expirado."))
                .andExpect(jsonPath("$.path").value("/api/orders"));
    }

    @Test
    void shouldAllowCustomerWithValidTokenToListOrders() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                UUID.randomUUID(),
                "customer@email.com",
                UserRole.CUSTOMER
        );

        when(tokenService.validateToken("customer-token"))
                .thenReturn(authenticatedUser);
        when(listOrdersService.findAll(any()))
                .thenReturn(new PageResponse<OrderResponse>(
                        List.of(),
                        0,
                        20,
                        0L,
                        0
                ));

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer customer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void shouldReturnForbiddenWhenCustomerTriesToCreateProduct() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                UUID.randomUUID(),
                "customer@email.com",
                UserRole.CUSTOMER
        );

        when(tokenService.validateToken("customer-token"))
                .thenReturn(authenticatedUser);

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer customer-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Produto Teste",
                                  "description": "Descrição",
                                  "price": 100.00,
                                  "stockQuantity": 10
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Você não tem permissão para acessar este recurso."))
                .andExpect(jsonPath("$.path").value("/api/products"));
    }

    @Test
    void shouldAllowAdminToCreateProduct() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                UUID.randomUUID(),
                "admin@email.com",
                UserRole.ADMIN
        );
        ProductResponse productResponse = new ProductResponse(
                UUID.randomUUID(),
                "Produto Teste",
                "Descrição",
                BigDecimal.valueOf(100),
                10,
                true,
                Instant.now(),
                Instant.now()
        );

        when(tokenService.validateToken("admin-token"))
                .thenReturn(authenticatedUser);
        when(createProductService.create(any()))
                .thenReturn(productResponse);

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Produto Teste",
                                  "description": "Descrição",
                                  "price": 100.00,
                                  "stockQuantity": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Produto Teste"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldNotBlockDocsEndpointWithSecurity() throws Exception {
        int status = mockMvc.perform(get("/docs"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status).isNotIn(401, 403);
    }

    @Test
    void shouldNotBlockSwaggerUiEndpointWithSecurity() throws Exception {
        int status = mockMvc.perform(get("/swagger-ui/index.html"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status).isNotIn(401, 403);
    }

    @Test
    void shouldNotBlockActuatorHealthEndpointWithSecurity() throws Exception {
        int status = mockMvc.perform(get("/actuator/health"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status).isNotIn(401, 403);
    }
}
