package net.alex.game.queue.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PasswordIn implements PasswordConfirmation {
    private String password;
    private String matchingPassword;
}
