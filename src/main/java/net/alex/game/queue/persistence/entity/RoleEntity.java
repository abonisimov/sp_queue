package net.alex.game.queue.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.alex.game.queue.persistence.RoleResource;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "role", uniqueConstraints = {@UniqueConstraint(columnNames = { "name", "resourceName", "resourceId" })})
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

    @Size(max=50)
    private String resourceName;

    @Size(max=50)
    private String resourceId;

    private long rank;

    public RoleEntity(final String name, String resourceName, String resourceId, long rank) {
        this.name = name;
        this.resourceName = resourceName;
        this.resourceId = resourceId;
        this.rank = rank;
    }

    public RoleEntity(final String name, RoleResource roleResource, long rank) {
        this.name = name;
        if (roleResource != null) {
            this.resourceName = roleResource.getName();
            this.resourceId = roleResource.getId();
        }
        this.rank = rank;
    }

    public RoleResource getRoleResource() {
        if (resourceName != null && resourceId != null) {
            return new RoleResource(resourceName, resourceId);
        } else {
            return null;
        }
    }

    public void setRoleResource(final RoleResource roleResource) {
        if (roleResource != null) {
            this.resourceName = roleResource.getName();
            this.resourceId = roleResource.getId();
        }
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
                "resourceName = " + resourceName + ", " +
                "resourceId = " + resourceId + ")";
    }
}