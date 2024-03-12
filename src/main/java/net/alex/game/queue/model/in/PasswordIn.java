package net.alex.game.queue.model.in;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.alex.game.queue.model.PasswordConfirmation;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PasswordIn implements PasswordConfirmation {
    private String password;
    private String matchingPassword;
}
