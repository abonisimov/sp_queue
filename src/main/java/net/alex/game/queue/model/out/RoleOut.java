package net.alex.game.queue.model.out;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleOut {
    private String name;
    private Long resourceId;
}
