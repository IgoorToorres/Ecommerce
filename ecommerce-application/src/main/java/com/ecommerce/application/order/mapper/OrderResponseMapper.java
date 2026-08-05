package com.ecommerce.application.order.mapper;

import com.ecommerce.application.order.response.OrderItemResponse;
import com.ecommerce.application.order.response.OrderResponse;
import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderResponseMapper {

    public OrderResponse toResponse(Order order){
        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for(OrderItem item : order.getItems()){
            itemResponses
                .add(new OrderItemResponse(
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

    public List<OrderResponse> toResponseList(List<Order> orders) {
        List<OrderResponse> responses = new ArrayList<>();

        for (Order order : orders) {
            responses.add(toResponse(order));
        }

        return responses;
    }
}
