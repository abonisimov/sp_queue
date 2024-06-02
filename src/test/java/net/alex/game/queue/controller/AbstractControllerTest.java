package net.alex.game.queue.controller;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.AbstractUserTest;
import net.alex.game.queue.model.in.RoleIn;
import net.alex.game.queue.persistence.RoleName;
import net.alex.game.queue.persistence.RoleResource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static net.alex.game.queue.persistence.RoleName.values;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractControllerTest extends AbstractUserTest {
    protected void checkWithPermissions(Set<RoleIn> roles, MVCExecutable executable) throws Exception {
        checkAccessGranted(roles, executable);
        checkAccessDenied(roles, executable);
    }

    private void checkAccessGranted(Set<RoleIn> roles, MVCExecutable executable) throws Exception {
        for (RoleIn role : roles) {
            cleanUserRecords();
            log.info("Checking access granted for role {}", role);
            String token = createTokenWithRole(role);
            executable.execute(token);
        }
    }

    private void checkAccessDenied(Set<RoleIn> roles, MVCExecutable executable) {
        Map<RoleResource, String> uniqueResources = new HashMap<>();
        SortedSet<RoleIn> sortedRoles = new TreeSet<>(Comparator.comparing(r -> RoleName.valueOf(r.getName()).getRank()));
        sortedRoles.addAll(roles);
        for (RoleIn role : sortedRoles) {
            if (role.getRoleResource() != null) {
                uniqueResources.put(role.getRoleResource(), role.getName());
            }
        }

        for (RoleResource resource : uniqueResources.keySet()) {
            RoleName minRole = RoleName.valueOf(uniqueResources.get(resource));
            List<RoleName> lowerRoles = Arrays.stream(values()).filter(r -> r.getRank() > minRole.getRank()).toList();

            for (RoleName roleName : lowerRoles) {
                cleanUserRecords();
                RoleIn role;
                if (roleName.isResourceIdRequired()) {
                    role = RoleIn.builder().name(roleName.name()).roleResource(resource).build();
                } else {
                    role = RoleIn.builder().name(roleName.name()).build();
                }

                log.info("Checking access denied for role {}", role);
                String token = createTokenWithRole(role);
                assertThrows(AssertionError.class, () -> executable.execute(token));
            }
        }
    }
}
