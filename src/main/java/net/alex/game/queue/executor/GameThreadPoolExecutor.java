package net.alex.game.queue.executor;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.event.InitStatisticsEvent;
import net.alex.game.queue.event.QueueTerminationEvent;
import net.alex.game.queue.exception.WaitingInterruptedException;
import net.alex.game.queue.serialize.EventSerializer;
import net.alex.game.queue.thread.GameEventThread;
import net.alex.game.queue.thread.GameThreadStats;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GameThreadPoolExecutor extends ThreadPoolExecutor {
    private final List<GameEventThread> activeTasks = Collections.synchronizedList(new ArrayList<>());

    public GameThreadPoolExecutor(int corePoolSize, long loadFactorPrecision) {
        super(corePoolSize, corePoolSize, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        for (int i = 0; i < corePoolSize; i++) {
            GameEventThread gameEventThread = new GameEventThread(new GameEventExecutor(), loadFactorPrecision);
            activeTasks.add(gameEventThread);
            execute(gameEventThread);
            gameEventThread.addEvent(InitStatisticsEvent.builder().id(UUID.randomUUID().toString()).build());
        }
    }

    public GameEventThread getVacantThread() {
        return activeTasks.stream().
                min(Comparator.comparingDouble(e -> e.getStatistics().getMomentaryLoadFactor())).
                orElseThrow();
    }

    public List<GameThreadStats> getThreadStatisticsList() {
        return activeTasks.stream().map(GameEventThread::getStatistics).toList();
    }

    public void shutdown(EventSerializer eventSerializer) {
        try{
            for (GameEventThread runnable : activeTasks) {
                stopThread(runnable, eventSerializer);
            }
        } catch (InterruptedException e) {
            log.warn("Waiting thread to stop was interrupted");
            log.warn(e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new WaitingInterruptedException();
        }
        super.shutdown();
    }

    private synchronized void stopThread(GameEventThread runnable, EventSerializer eventSerializer) throws InterruptedException {
        log.debug("Stopping queue thread");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        runnable.addEvent(QueueTerminationEvent.
                builder().
                id(UUID.randomUUID().toString()).
                delay(0).
                timeUnit(TimeUnit.MILLISECONDS).
                eventSerializer(eventSerializer).
                shutdownLatch(countDownLatch).build());
        countDownLatch.await();
        log.debug("Queue thread stopped");
    }
}
