package com.ecommerce.application.order.command;

import java.util.UUID;

public record CreateOrderItemCommand (
    UUID productId,
    int quantity
){}
