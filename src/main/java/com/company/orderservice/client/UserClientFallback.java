package com.company.orderservice.client;

import com.company.orderservice.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserDto getUserById(Long id) {
        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(id);
        fallbackUser.setName("Temporary Fallback User");
        fallbackUser.setEmail("service-unavailable@local");
        return fallbackUser;
    }
}