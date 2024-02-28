package net.alex.game.queue.service;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.model.event.GameEvent;
import net.alex.game.queue.exception.EventDeclinedException;
import net.alex.game.queue.executor.GameThreadPoolExecutor;
import net.alex.game.queue.thread.GameEventThread;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EventService {
    private final GameThreadPoolExecutor threadPoolExecutor;

    public EventService(GameThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public void addEvent(GameEvent gameEvent) {
        checkAndAddEvent(gameEvent);
    }

    public void addEvent(String universeId, String eventId, long delay, TimeUnit timeUnit) {
        checkAndAddEvent(GameEvent.builder().universeId(universeId).id(eventId).delay(delay).timeUnit(timeUnit).build());
    }

    private void checkAndAddEvent(GameEvent gameEvent) {
        GameEventThread gameEventThread = threadPoolExecutor.getVacantThread();
        if (gameEventThread != null) {
            gameEventThread.addEvent(gameEvent);
        } else {
            log.debug("Can't access thread to pass event into the queue, universeId {}, eventId {}",
                    gameEvent.getUniverseId(), gameEvent.getId());
            throw new EventDeclinedException();
        }
    }
}
