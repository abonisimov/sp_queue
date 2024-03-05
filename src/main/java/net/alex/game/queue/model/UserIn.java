package net.alex.game.queue.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.alex.game.queue.annotation.PasswordMatches;
import net.alex.game.queue.annotation.ValidEmail;
import net.alex.game.queue.annotation.ValidPassword;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@PasswordMatches
public class UserIn {
    @NotNull(message = "First name is mandatory")
    @Size(min = 2, max=255, message = "First name length must be greater than {min} and less than {max}")
    private String firstName;

    @NotNull(message = "Last name is mandatory")
    @Size(min = 2, max=255, message = "Last name length must be greater than {min} and less than {max}")
    private String lastName;

    @NotNull(message = "Nick name is mandatory")
    @Size(min = 4, max=255, message = "Nick name length must be greater than {min} and less than {max}")
    private String nickName;

    @ValidPassword
    private String password;

    @NotNull(message = "Password confirmation is mandatory")
    @Size(min = 1)
    private String matchingPassword;

    @ValidEmail
    @NotNull(message = "Email name is mandatory")
    @Size(min = 5, max=255, message = "Email length must be greater than {min} and less than {max}")
    private String email;
}
