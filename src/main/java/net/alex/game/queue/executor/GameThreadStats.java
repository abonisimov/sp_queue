package net.alex.game.queue.executor;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@EqualsAndHashCode
@Getter
@SuperBuilder
public class GameThreadStats {
    private long startTime;
    private long operationsDone;
    private long operationsFailed;
    private long totalExecutionTime;
    private long totalWaitTime;
    private double loadFactor;

    private double averageExecutionTime;
    private double averageWaitTime;

    private long lastXStartTime;
    private long currentXOperationsCount;
    private long momentaryLoadFactorPrecision;
    private long lastXExecutionTime;
    private long lastXWaitTime;
    private double momentaryLoadFactor;

    public static GameThreadStats updateStatsAndGet(GameThreadStats prevStats, 
                                                    long threadStartTime,
                                                    long cycleStartTime,
                                                    long cycleEndTime,
                                                    boolean isSuccessCycle,
                                                    long loadFactorPrecision) {
        GameThreadStats updatedStats = new GameThreadStats();
        updatedStats.startTime = threadStartTime;
        updatedStats.momentaryLoadFactorPrecision = loadFactorPrecision;
        if (isSuccessCycle) {
            updatedStats.operationsDone = prevStats.getOperationsDone() + 1;
            updatedStats.operationsFailed = prevStats.getOperationsFailed();
        } else {
            updatedStats.operationsFailed = prevStats.getOperationsFailed() + 1;
            updatedStats.operationsDone = prevStats.getOperationsDone();
        }
        long totalTime = cycleEndTime - threadStartTime;
        long lastXTotalTime = cycleEndTime - (prevStats.getLastXStartTime() != 0 ? prevStats.getLastXStartTime() : threadStartTime);
        long totalOperations = updatedStats.getOperationsDone() + updatedStats.getOperationsFailed();
        long cycleTime = cycleEndTime - cycleStartTime;
        if ((totalOperations % loadFactorPrecision) == 0) {
            if (lastXTotalTime != 0) {
                updatedStats.momentaryLoadFactor = (prevStats.getLastXExecutionTime() + cycleTime) / (double) lastXTotalTime * 100;
            } else{
                updatedStats.momentaryLoadFactor = 0;
            }
            updatedStats.lastXStartTime = cycleStartTime;
            updatedStats.lastXExecutionTime = 0;
            updatedStats.lastXWaitTime = 0;
            updatedStats.currentXOperationsCount = 0;
        } else {
            updatedStats.lastXExecutionTime = prevStats.getLastXExecutionTime() + cycleTime;
            updatedStats.lastXWaitTime = lastXTotalTime - updatedStats.getLastXExecutionTime();
            updatedStats.currentXOperationsCount = prevStats.getCurrentXOperationsCount() + 1;
        }
        updatedStats.totalExecutionTime = prevStats.getTotalExecutionTime() + cycleTime;
        updatedStats.totalWaitTime = totalTime - updatedStats.getTotalExecutionTime();
        if (totalOperations != 0) {
            updatedStats.averageExecutionTime = updatedStats.getTotalExecutionTime() / (double) totalOperations;
            updatedStats.averageWaitTime = updatedStats.getTotalWaitTime() / (double) totalOperations;
        } else {
            updatedStats.averageExecutionTime = 0;
            updatedStats.averageWaitTime = updatedStats.getTotalWaitTime();
        }
        if (totalTime != 0) {
            updatedStats.loadFactor = updatedStats.getTotalExecutionTime() / (double) totalTime * 100;
        } else {
            updatedStats.loadFactor = 0;
        }
        return updatedStats;
    }
}
