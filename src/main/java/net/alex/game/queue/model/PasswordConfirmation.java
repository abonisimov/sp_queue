package net.alex.game.queue.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.alex.game.queue.annotation.PasswordMatches;
import net.alex.game.queue.annotation.ValidPassword;

@PasswordMatches
public interface PasswordConfirmation {
    @ValidPassword
    String getPassword();

    @NotNull(message = "Password confirmation is mandatory")
    @Size(min = 1)
    String getMatchingPassword();
}
