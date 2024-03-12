package net.alex.game.queue.model.in;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class UserIn {
    @NotNull(message = "First name is mandatory")
    @Size(min = 2, max=50, message = "First name length must be greater than {min} and less than {max}")
    private String firstName;

    @NotNull(message = "Last name is mandatory")
    @Size(min = 2, max=50, message = "Last name length must be greater than {min} and less than {max}")
    private String lastName;

    @NotNull(message = "Nick name is mandatory")
    @Size(min = 4, max=50, message = "Nick name length must be greater than {min} and less than {max}")
    private String nickName;
}
