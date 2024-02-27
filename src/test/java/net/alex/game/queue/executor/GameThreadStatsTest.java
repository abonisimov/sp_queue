package net.alex.game.queue.executor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GameThreadStatsTest {

    @Test
    void testUpdateStatsAndGet() {
        GameThreadStats stats = new GameThreadStats();
        long cycleEndTime = System.currentTimeMillis();
        long startTime = cycleEndTime - 100;
        long cycleStartTime = cycleEndTime - 30;
        stats = GameThreadStats.updateStatsAndGet(stats, startTime, cycleStartTime, cycleEndTime, true, 10);

        assertNotNull(stats);
        assertEquals(GameThreadStats.builder()
                .startTime(startTime)
                .lastXStartTime(0)
                .operationsDone(1)
                .operationsFailed(0)
                .totalExecutionTime(30)
                .totalWaitTime(70)
                .currentXOperationsCount(1)
                .momentaryLoadFactorPrecision(10)
                .lastXExecutionTime(30)
                .lastXWaitTime(70)
                .averageExecutionTime(30.0)
                .averageWaitTime(70.0)
                .loadFactor(30.0)
                .momentaryLoadFactor(0.0)
                .build(), stats);

        for (int i = 0; i < 9; i++) {
            cycleStartTime = cycleEndTime + 70;
            cycleEndTime += 100;
            stats = GameThreadStats.updateStatsAndGet(stats, startTime, cycleStartTime, cycleEndTime, false, 10);

            if (i == 5) {
                assertNotNull(stats);
                assertEquals(GameThreadStats.builder()
                        .startTime(startTime)
                        .lastXStartTime(0)
                        .operationsDone(1)
                        .operationsFailed(6)
                        .totalExecutionTime(210)
                        .totalWaitTime(490)
                        .currentXOperationsCount(7)
                        .momentaryLoadFactorPrecision(10)
                        .lastXExecutionTime(210)
                        .lastXWaitTime(490)
                        .averageExecutionTime(30.0)
                        .averageWaitTime(70.0)
                        .loadFactor(30.0)
                        .momentaryLoadFactor(0.0)
                        .build(), stats);
            }
        }

        assertNotNull(stats);
        assertEquals(GameThreadStats.builder()
                .startTime(startTime)
                .lastXStartTime(cycleStartTime)
                .operationsDone(1)
                .operationsFailed(9)
                .totalExecutionTime(300)
                .totalWaitTime(700)
                .currentXOperationsCount(0)
                .momentaryLoadFactorPrecision(10)
                .lastXExecutionTime(0)
                .lastXWaitTime(0)
                .averageExecutionTime(30.0)
                .averageWaitTime(70.0)
                .loadFactor(30.0)
                .momentaryLoadFactor(30.0)
                .build(), stats);
    }

}