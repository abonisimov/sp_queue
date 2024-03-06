package net.alex.game.queue.model;

public enum UserStatus {
    ENABLE(true),
    DISABLE(false);

    private final boolean enabled;

    UserStatus(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
