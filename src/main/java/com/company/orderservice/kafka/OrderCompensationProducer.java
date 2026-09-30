package com.company.orderservice.kafka;

import com.company.orderservice.event.OrderCompensationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCompensationProducer {

    private static final String TOPIC = "order-compensation";

    private final KafkaTemplate<String, OrderCompensationEvent> kafkaTemplate;

    public void publishCompensationEvent(
            OrderCompensationEvent event) {

        kafkaTemplate.send(
                TOPIC,
                String.valueOf(event.getOrderId()),
                event
        );
    }
}