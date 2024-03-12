package net.alex.game.queue.model.in;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.alex.game.queue.model.UserStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusIn {
    @NotNull(message = "Status is mandatory")
    private UserStatus userStatus;
}
