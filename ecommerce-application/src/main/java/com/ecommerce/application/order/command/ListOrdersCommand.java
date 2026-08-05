package com.ecommerce.application.order.command;

import com.ecommerce.domain.order.OrderStatus;

import java.util.UUID;

public record ListOrdersCommand (
        UUID customerId,
        OrderStatus status,
        int page,
        int size
){}
