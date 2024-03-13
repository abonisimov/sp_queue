package net.alex.game.queue.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "role", uniqueConstraints = {@UniqueConstraint(columnNames = { "name", "resourceId" })})
public class RoleEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 6418907335913021997L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(mappedBy = "roles")
    private Collection<UserEntity> users;

    @NotNull
    @Size(max=25)
    private String name;

    private Long resourceId;

    private long rank;

    public RoleEntity(final String name, Long resourceId, long rank) {
        this.name = name;
        this.resourceId = resourceId;
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        RoleEntity that = (RoleEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "name = " + name + ", " +
                "resourceId = " + resourceId + ")";
    }
}