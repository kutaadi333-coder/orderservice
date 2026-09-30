package com.company.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompensationEvent {

    private Long orderId;

    private String reason;

    private String eventType;
}