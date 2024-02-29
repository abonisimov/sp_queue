package net.alex.game.queue.executor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.alex.game.model.event.GameEvent;
import net.alex.game.queue.event.InitStatisticsEvent;
import net.alex.game.queue.event.QueueTerminationEvent;
import net.alex.game.queue.exception.WaitingInterruptedException;
import net.alex.game.queue.serialize.EventSerializer;
import net.alex.game.queue.thread.GameEventThread;
import net.alex.game.queue.thread.GameThreadStats;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
public class GameThreadPoolExecutor extends ThreadPoolExecutor {
    private final List<GameEventThread> activeTasks = Collections.synchronizedList(new ArrayList<>());

    private final long loadFactorPrecision;
    private final EventSerializer eventSerializer;
    private final Random random = new Random();

    public GameThreadPoolExecutor(int corePoolSize,
                                  long loadFactorPrecision,
                                  EventSerializer eventSerializer) {
        super(corePoolSize, corePoolSize, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.loadFactorPrecision = loadFactorPrecision;
        this.eventSerializer = eventSerializer;
    }

    public GameEventThread getVacantThread() {
        return activeTasks.stream().
                min(Comparator.comparingDouble(e -> e.getStatistics().getMomentaryLoadFactor())).
                orElseThrow();
    }

    public List<GameThreadStats> getThreadStatisticsList() {
        return activeTasks.stream().map(GameEventThread::getStatistics).toList();
    }

    @PostConstruct
    private void startUp() {
        try {
            startAllThreads();
            eventSerializer.readEvents(event -> activeTasks.get(random.nextInt(activeTasks.size())).addEvent(event));
        } catch (IOException e) {
            log.warn("Unable to restore events from the database");
            log.warn(e.getMessage(), e);
        }
    }

    private void startAllThreads() {
        log.info("Starting thread pool of size {}, load factor precision {}", getCorePoolSize(), loadFactorPrecision);
        for (int i = 0; i < getCorePoolSize(); i++) {
            GameEventThread gameEventThread = new GameEventThread(new GameEventExecutor(), loadFactorPrecision);
            activeTasks.add(gameEventThread);
            execute(gameEventThread);
            gameEventThread.addEvent(InitStatisticsEvent.builder().id(UUID.randomUUID().toString()).build());
        }
    }

    @PreDestroy
    private void shutdownAndWait() throws InterruptedException {
        log.info("Shutting down thread pool");
        try{
            Stream.Builder<GameEvent> builder = Stream.builder();
            for (GameEventThread runnable : activeTasks) {
                stopThread(runnable);
                collectEventsFromQueue(builder, runnable);
            }
            writeEvents(builder.build());
        } catch (InterruptedException e) {
            log.warn("Waiting thread to stop was interrupted");
            log.warn(e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new WaitingInterruptedException();
        }
        super.shutdown();
        while (!awaitTermination(1, TimeUnit.SECONDS)) {
            log.debug("Awaiting all game threads to finish");
        }
    }

    private void collectEventsFromQueue(Stream.Builder<GameEvent> builder, GameEventThread runnable) {
        Iterator<GameEvent> iterator = runnable.getQueueIterator();
        while (iterator.hasNext())
            builder.accept(iterator.next());
    }

    private void writeEvents(Stream<GameEvent> stream) {
        try {
            eventSerializer.writeEvents(stream.iterator());
        } catch (IOException e) {
            log.warn("Unable to backup queue because of the following reason:");
            log.warn(e.getMessage(), e);
        }
    }

    private void stopThread(GameEventThread runnable) throws InterruptedException {
        log.debug("Stopping queue thread");
        runnable.addEvent(QueueTerminationEvent.
                builder().
                id(UUID.randomUUID().toString()).
                delay(0).
                timeUnit(TimeUnit.MILLISECONDS).
                build());
        log.debug("Queue thread stopped");
    }
}
