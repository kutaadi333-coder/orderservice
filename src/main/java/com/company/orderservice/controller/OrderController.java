package com.company.orderservice.controller;

import com.company.orderservice.dto.CreateOrderRequest;
import com.company.orderservice.dto.OrderResponse;
import com.company.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Logger logger =
            LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        logger.info(
                "Request started: Create order for userId={}",
                request.getUserId()
        );

        OrderResponse response =
                orderService.createOrder(request);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id) {

        logger.info(
                "Request started: Get order id={}",
                id
        );

        return ResponseEntity.ok(
                orderService.getOrderById(id)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(
            @PathVariable Long userId) {

        logger.info(
                "Request started: Get orders for userId={}",
                userId
        );

        return ResponseEntity.ok(
                orderService.getOrdersByUserId(userId)
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        logger.info(
                "Request started: Update order status for orderId={}",
                id
        );

        return ResponseEntity.ok(
                orderService.updateOrderStatus(id, status)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long id) {

        logger.info(
                "Request started: Cancel order id={}",
                id
        );

        orderService.cancelOrder(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/compensate")
    public ResponseEntity<OrderResponse> requestCompensation(
            @PathVariable Long id,
            @RequestParam(
                    defaultValue = "Distributed transaction compensation"
            )
            String reason) {

        logger.info(
                "Request started: Compensation for orderId={}, reason={}",
                id,
                reason
        );

        return ResponseEntity.ok(
                orderService.requestCompensation(
                        id,
                        reason
                )
        );
    }

    @PostMapping("/{id}/saga")
    public ResponseEntity<OrderResponse> requestSaga(
            @PathVariable Long id,
            @RequestParam(
                    defaultValue = "false"
            )
            boolean simulateFailure) {

        logger.info(
                "Request started: Saga for orderId={}, simulateFailure={}",
                id,
                simulateFailure
        );

        return ResponseEntity.ok(
                orderService.requestSaga(
                        id,
                        simulateFailure
                )
        );
    }
}