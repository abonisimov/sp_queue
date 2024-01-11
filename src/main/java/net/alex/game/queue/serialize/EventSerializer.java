package net.alex.game.queue.serialize;

import net.alex.game.queue.event.GameEvent;

import java.util.Iterator;
import java.util.function.Consumer;

public interface EventSerializer {
    void readEvents(long universeId, Consumer<GameEvent> supplier);
    void writeEvents(long universeId, Iterator<GameEvent> iterator);
}
