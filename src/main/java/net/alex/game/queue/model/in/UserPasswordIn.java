package net.alex.game.queue.model.in;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.alex.game.queue.model.PasswordConfirmation;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class UserPasswordIn extends UserIn implements PasswordConfirmation {
    private String password;
    private String matchingPassword;
}
