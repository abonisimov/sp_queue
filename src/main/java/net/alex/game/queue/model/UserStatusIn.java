package net.alex.game.queue.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusIn {
    @NotNull(message = "Status is mandatory")
    private UserStatus userStatus;
}
