package com.company.orderservice.client;

import com.company.orderservice.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    private static final Logger logger =
            LoggerFactory.getLogger(UserClientFallback.class);

    @Override
    public UserDto getUserById(Long id) {

        logger.warn(
                "Circuit Breaker fallback triggered for userId={}",
                id
        );

        return null;
    }
}