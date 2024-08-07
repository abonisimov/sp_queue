package net.alex.game.queue.model.out;

import lombok.Builder;
import lombok.Data;
import net.alex.game.queue.persistence.RoleResource;
import net.alex.game.queue.persistence.entity.RoleEntity;

@Data
@Builder
public class RoleOut implements Comparable<RoleOut> {
    private long id;
    private String name;
    private RoleResource roleResource;
    private long rank;

    public static RoleOut fromEntity(RoleEntity entity) {
        return RoleOut.
                builder().
                id(entity.getId()).
                name(entity.getName()).
                roleResource(entity.getRoleResource()).
                rank(entity.getRank()).
                build();
    }

    @Override
    public int compareTo(RoleOut that) {
        return Long.compare(this.getRank(), that.getRank());
    }
}
