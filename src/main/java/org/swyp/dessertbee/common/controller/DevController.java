package org.swyp.dessertbee.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dev")
public class DevController {

    private final UserRepository userRepository;

    public DevController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        List<UserEntity> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (UserEntity user : users) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("email", user.getEmail());
            userMap.put("name", user.getName());
            userMap.put("nickname", user.getNickname());
            userMap.put("uuid", user.getUserUuid().toString()); // UUID를 문자열로 변환
            userMap.put("role", user.getUserRoles().stream()
                    .map(r -> r.getRole().getName().name())
                    .collect(Collectors.toList()));

            result.add(userMap);
        }

        return result;
    }
}