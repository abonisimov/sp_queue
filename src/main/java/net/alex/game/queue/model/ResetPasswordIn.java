package net.alex.game.queue.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ResetPasswordIn implements PasswordConfirmation {

    @NotNull(message = "Email is mandatory")
    private String email;

    @NotNull(message = "Old password is required to update to a new one")
    private String oldPassword;

    private String password;
    private String matchingPassword;
}
