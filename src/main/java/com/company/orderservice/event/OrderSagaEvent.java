package com.company.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSagaEvent {

    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String eventType;
    private String status;
    private String reason;
}