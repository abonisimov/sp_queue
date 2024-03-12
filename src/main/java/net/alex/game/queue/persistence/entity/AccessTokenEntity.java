package net.alex.game.queue.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@Table(name = "access_token")
public class AccessTokenEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 5565423312020725844L;

    private static final int TOKEN_TTL_DAYS = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max=50)
    @Column(unique = true)
    private String token;

    @OneToOne(targetEntity = UserEntity.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id", foreignKey = @ForeignKey(name = "fk_access_token_on_user"))
    private UserEntity user;

    @NotNull
    private LocalDateTime expiryDate;

    public AccessTokenEntity() {
        this.expiryDate = calculateExpiryDate();
    }

    public AccessTokenEntity(final String token, final UserEntity user) {
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate();
    }

    public void updateToken(final String token) {
        this.token = token;
        this.expiryDate = calculateExpiryDate();
    }

    private LocalDateTime calculateExpiryDate() {
        return LocalDateTime.now().plusDays(TOKEN_TTL_DAYS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AccessTokenEntity that = (AccessTokenEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
