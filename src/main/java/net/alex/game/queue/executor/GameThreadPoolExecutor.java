package net.alex.game.queue.executor;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GameThreadPoolExecutor extends ThreadPoolExecutor {
    private final ConcurrentHashMap<Long, GameEventThread> activeTasks = new ConcurrentHashMap<>();

    public GameThreadPoolExecutor(int corePoolSize,
                                  int maximumPoolSize,
                                  long keepAliveTime,
                                  TimeUnit unit,
                                  BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        GameEventThread runnable = (GameEventThread)r;
        activeTasks.put(runnable.getUniverseId(), runnable);
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        activeTasks.remove(((GameEventThread)r).getUniverseId());
    }

    public Set<Long> getUniverseSet() {
        return Collections.unmodifiableSet(activeTasks.keySet());
    }

    public GameEventThread getTask(long universeId) {
        return activeTasks.get(universeId);
    }

    public boolean isUniversePresent(long universeId) {
        return activeTasks.containsKey(universeId);
    }

    public boolean hasCapacity() {
        return getMaximumPoolSize() > getActiveCount();
    }
}
