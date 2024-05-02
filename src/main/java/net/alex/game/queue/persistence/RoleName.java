package net.alex.game.queue.persistence;

import lombok.Getter;

public enum RoleName {
    SYSTEM(0, false),
    ROOT(10, false),
    ADMIN(100, false),
    OWNER(1000, true),
    MEMBER(2000, true),
    WATCHER(3000, true),
    USER(4000, false);

    @Getter
    private final long rank;
    @Getter
    private final boolean resourceIdRequired;

    RoleName(long rank, boolean resourceIdRequired) {
        this.rank = rank;
        this.resourceIdRequired = resourceIdRequired;
    }
}
