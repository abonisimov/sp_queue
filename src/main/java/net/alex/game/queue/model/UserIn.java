package net.alex.game.queue.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.alex.game.queue.annotation.ValidEmail;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class UserIn {
    @NotNull(message = "First name is mandatory")
    @Size(min = 2, max=255, message = "First name length must be greater than {min} and less than {max}")
    private String firstName;

    @NotNull(message = "Last name is mandatory")
    @Size(min = 2, max=255, message = "Last name length must be greater than {min} and less than {max}")
    private String lastName;

    @Size(min = 4, max=255, message = "Nick name length must be greater than {min} and less than {max}")
    private String nickName;

    @ValidEmail
    @NotNull(message = "Email name is mandatory")
    @Size(min = 5, max=255, message = "Email length must be greater than {min} and less than {max}")
    private String email;
}
