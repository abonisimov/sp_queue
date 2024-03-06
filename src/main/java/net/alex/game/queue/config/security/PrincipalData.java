package net.alex.game.queue.config.security;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.alex.game.queue.persistence.entity.UserEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@EqualsAndHashCode
public class PrincipalData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1980129964325124351L;

    private long userId;
    private String email;
    private String nickName;
    private String firstName;
    private String lastName;
    private LocalDateTime lastLogin;

    public static PrincipalData fromUserEntity(UserEntity userEntity) {
        return PrincipalData.builder().
                userId(userEntity.getId()).
                email(userEntity.getEmail()).
                nickName(userEntity.getNickName()).
                firstName(userEntity.getFirstName()).
                lastName(userEntity.getLastName()).
                lastLogin(userEntity.getLastLogin()).
                build();
    }
}
