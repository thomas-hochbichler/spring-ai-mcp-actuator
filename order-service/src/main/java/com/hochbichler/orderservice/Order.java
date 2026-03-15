package com.hochbichler.orderservice;

public record Order(String id, String customer, String product, double amount, String status) {
}
