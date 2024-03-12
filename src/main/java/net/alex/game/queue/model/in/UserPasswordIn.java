package net.alex.game.queue.model.in;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.alex.game.queue.annotation.ValidEmail;
import net.alex.game.queue.model.PasswordConfirmation;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class UserPasswordIn extends UserIn implements PasswordConfirmation {

    @ValidEmail
    @NotNull(message = "Email name is mandatory")
    @Size(min = 5, max=255, message = "Email length must be greater than {min} and less than {max}")
    private String email;

    private String password;
    private String matchingPassword;
}
