package net.alex.game.queue.model.in;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.alex.game.queue.annotation.ValidRole;
import net.alex.game.queue.persistence.RoleResource;

@Data
@Valid
@ValidRole
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
public class RoleIn {

    @NotNull(message = "Role name is mandatory")
    private String name;
    private RoleResource roleResource;
}
