package com.ecommerce.application.order.service;

import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.application.order.command.CreateOrderCommand;
import com.ecommerce.application.order.command.CreateOrderItemCommand;
import com.ecommerce.application.order.repository.OrderRepository;
import com.ecommerce.application.order.response.OrderItemResponse;
import com.ecommerce.application.order.response.OrderResponse;
import com.ecommerce.application.product.repository.ProductRepository;
import com.ecommerce.domain.exception.DomainException;
import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderItem;
import com.ecommerce.domain.product.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CreateOrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public CreateOrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse create(CreateOrderCommand command) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (CreateOrderItemCommand itemCommand : command.items()) {
            Product product = productRepository.findById(itemCommand.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));

            if (!product.isActive()) {
                throw new DomainException("Produto inativo não pode ser adicionado ao pedido.");
            }

            if (!product.hasAvailableStock(itemCommand.quantity())) {
                throw new DomainException("Estoque insuficiente.");
            }

            product.decreaseStock(itemCommand.quantity());

            OrderItem orderItem = new OrderItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    itemCommand.quantity()
            );

            orderItems.add(orderItem);
        }

        Order order = new Order(command.customerId(), orderItems);
        Order savedOrder = orderRepository.save(order);

        return toResponse(savedOrder);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            itemResponses.add(new OrderItemResponse(
                    item.getId(),
                    item.getProductId(),
                    item.getProductName(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    item.getTotalPrice()
            ));
        }

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getPaidAt(),
                order.getCancelledAt(),
                order.getShippedAt(),
                order.getDeliveredAt(),
                itemResponses
        );
    }
}
