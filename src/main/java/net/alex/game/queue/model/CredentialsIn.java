package net.alex.game.queue.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class CredentialsIn {
    @NotNull
    private String nickName;
    @NotNull
    private String password;
}
