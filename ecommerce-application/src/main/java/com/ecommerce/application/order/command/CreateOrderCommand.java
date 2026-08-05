package com.ecommerce.application.order.command;

import java.util.List;
import java.util.UUID;

public record CreateOrderCommand (
    UUID customerId,
    List<CreateOrderItemCommand> items
){}
