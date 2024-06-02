package net.alex.game.queue.persistence;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@Valid
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
@ToString
public class RoleResource {

    @NotNull(message = "Resource name is mandatory")
    @Size(min = 2, max=50, message = "Resource name length must be greater than {min} and less than {max}")
    private String name;

    @NotNull(message = "Resource id is mandatory")
    @Size(min = 2, max=50, message = "Resource id length must be greater than {min} and less than {max}")
    private String id;
}
