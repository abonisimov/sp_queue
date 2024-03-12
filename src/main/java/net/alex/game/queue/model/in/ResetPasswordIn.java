package net.alex.game.queue.model.in;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ResetPasswordIn extends PasswordIn {

    @NotNull(message = "Email is mandatory")
    private String email;

    @NotNull(message = "Old password is required to update to a new one")
    private String oldPassword;
}
