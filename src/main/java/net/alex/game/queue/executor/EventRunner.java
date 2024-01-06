package net.alex.game.queue.executor;

public interface EventRunner {
    void executeEvent(long universeId, String eventId);
}
