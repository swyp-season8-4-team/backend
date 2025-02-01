package org.swyp.dessertbee.userrole;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.role.entity.RoleEntity;
import org.swyp.dessertbee.role.repository.RoleRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN", "ROLE_OWNER");

        for (String roleName : roles) {
            if (roleRepository.findByName(roleName).isEmpty()) { // 없다면 추가, 있다면 추가하지 않음
                roleRepository.save(RoleEntity.builder().name(roleName).build());
                System.out.println("Seeded Role: " + roleName);
            }
        }
    }
}
