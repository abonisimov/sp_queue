package net.alex.game.queue.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "registration_token")
public class RegistrationTokenEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -4247710961221759320L;

    public static final int TOKEN_TTL_HOURS = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max=50)
    @Column(unique = true)
    private String token;

    @NotNull
    @Size(max=255)
    private String email;

    @NotNull
    private LocalDateTime expiryTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        RegistrationTokenEntity that = (RegistrationTokenEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
