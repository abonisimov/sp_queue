package net.alex.game.queue.service;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.exception.ThreadNotFoundException;
import net.alex.game.queue.executor.GameThreadPoolExecutor;
import net.alex.game.queue.thread.GameThreadStats;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ThreadService {
    private final GameThreadPoolExecutor threadPoolExecutor;

    public ThreadService(GameThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public GameThreadStats getThreadStatistics(long threadId) {
        return threadPoolExecutor.getThreadStatisticsList().stream().
                filter(s -> s.getThreadId() == threadId).findAny().
                orElseThrow(ThreadNotFoundException::new);
    }

    public List<GameThreadStats> getThreadStatisticsList() {
        return threadPoolExecutor.getThreadStatisticsList();
    }
}
