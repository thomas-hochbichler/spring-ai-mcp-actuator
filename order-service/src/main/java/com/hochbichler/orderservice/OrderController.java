package com.hochbichler.orderservice;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final List<Order> ORDERS = List.of(
        new Order("ORD-001", "Alice", "Laptop", 1299.99, "SHIPPED"),
        new Order("ORD-002", "Bob", "Keyboard", 89.99, "PROCESSING"),
        new Order("ORD-003", "Charlie", "Monitor", 549.00, "DELIVERED")
    );

    @GetMapping
    public List<Order> getAllOrders() {
        return ORDERS;
    }

    @GetMapping("/{id}")
    public Optional<Order> getOrder(@PathVariable String id) {
        return ORDERS.stream()
            .filter(order -> order.id().equals(id))
            .findFirst();
    }
}
