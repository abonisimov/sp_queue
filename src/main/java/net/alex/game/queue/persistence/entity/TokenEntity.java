package net.alex.game.queue.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@Table(name = "token")
public class TokenEntity {

    private static final int TOKEN_TTL_DAYS = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(targetEntity = UserEntity.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id", foreignKey = @ForeignKey(name = "FK_VERIFY_USER"))
    private UserEntity user;

    private LocalDateTime expiryDate;

    public TokenEntity() {
        this.expiryDate = calculateExpiryDate();
    }

    public TokenEntity(final String token, final UserEntity user) {
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
        TokenEntity that = (TokenEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
