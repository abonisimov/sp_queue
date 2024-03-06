package net.alex.game.queue.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.alex.game.queue.persistence.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@EqualsAndHashCode
public class UserOut {
    private long id;
    private String firstName;
    private String lastName;
    private String nickName;
    private String email;
    private boolean enabled;
    private List<RoleOut> roles;
    private LocalDateTime lastLogin;

    public static UserOut fromUserEntity(UserEntity userEntity) {
        return UserOut.builder()
                .id(userEntity.getId())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .nickName(userEntity.getNickName())
                .email(userEntity.getEmail())
                .roles(userEntity.getRoles().stream().map(e -> new RoleOut(e.getName(), e.getResourceId())).toList())
                .lastLogin(userEntity.getLastLogin())
                .enabled(userEntity.isEnabled())
                .build();
    }
}
