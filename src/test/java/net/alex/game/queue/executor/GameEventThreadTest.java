package net.alex.game.queue.executor;

import net.alex.game.queue.event.FastModeSwitchEvent;
import net.alex.game.queue.event.GameEvent;
import net.alex.game.queue.event.UniverseQueueTerminationEvent;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEventThreadTest {
    static Map<Long, String> eventIdToDuration = new LinkedHashMap<>();
    static {
        eventIdToDuration.put(100L, "1");
        eventIdToDuration.put(0L, "2");
        eventIdToDuration.put(400L, "3");
        eventIdToDuration.put(300L, "4");
    }

    @Test
    void testQueueInStandardMode() throws InterruptedException {
        long maxDuration = eventIdToDuration.keySet().stream().max(Long::compareTo).orElseThrow();
        long waitTime = maxDuration + 100;

        TestEventRunner eventRunner = new TestEventRunner();
        runEventThread(eventRunner,
                eventIdToDuration.entrySet().stream().
                        map(e -> new GameEvent(e.getValue(), e.getKey(), TimeUnit.MILLISECONDS)).
                        collect(Collectors.toList()),
                waitTime);

        checkEventsSequence(eventRunner);
        assertTrue(eventRunner.getDuration() >= maxDuration);
        assertTrue(eventRunner.getDuration() <= waitTime);
    }

    @Test
    void testQueueInFastMode() throws InterruptedException {
        long maxDuration = eventIdToDuration.keySet().stream().max(Long::compareTo).orElseThrow();
        long waitTime = maxDuration + 100;

        TestEventRunner eventRunner = new TestEventRunner();
        List<GameEvent> events = new ArrayList<>();
        events.add(new FastModeSwitchEvent(true, 0, TimeUnit.MILLISECONDS));
        events.addAll(eventIdToDuration.entrySet().stream().
                map(e -> new GameEvent(e.getValue(), e.getKey(), TimeUnit.MILLISECONDS)).toList());
        runEventThread(eventRunner, events, waitTime);

        checkEventsSequence(eventRunner);
        assertTrue(eventRunner.getDuration() < maxDuration);
    }

    @Test
    void testQueueInMixedMode() throws InterruptedException {
        long maxDuration = eventIdToDuration.keySet().stream().max(Long::compareTo).orElseThrow();
        long waitTime = maxDuration + 100;

        TestEventRunner eventRunner = new TestEventRunner();
        List<GameEvent> events = new ArrayList<>();
        events.add(new FastModeSwitchEvent(true, 50, TimeUnit.MILLISECONDS));
        events.add(new FastModeSwitchEvent(false, 350, TimeUnit.MILLISECONDS));
        events.addAll(eventIdToDuration.entrySet().stream().
                map(e -> new GameEvent(e.getValue(), e.getKey(), TimeUnit.MILLISECONDS)).toList());
        runEventThread(eventRunner, events, waitTime);

        checkEventsSequence(eventRunner);
        System.out.println(eventRunner.getDuration());
        System.out.println(maxDuration);
        assertTrue(eventRunner.getDuration() < maxDuration);
    }

    private void runEventThread(EventRunner eventRunner,
                                List<GameEvent> events,
                                long waitTime) throws InterruptedException {
        GameEventThread thread = new GameEventThread(1, new CountDownLatch(1), eventRunner);
        new Thread(thread).start();
        events.forEach(thread::addEvent);
        Thread.sleep(waitTime);
        thread.addEvent(new UniverseQueueTerminationEvent(new CountDownLatch(1)));
    }

    private void checkEventsSequence(TestEventRunner eventRunner) {
        List<String> expectedEventSequence = eventIdToDuration.entrySet().stream().
                sorted(Comparator.comparingLong(Map.Entry::getKey)).map(Map.Entry::getValue).toList();
        assertEquals(expectedEventSequence, eventRunner.getEventSequence());
    }

    static class TestEventRunner implements EventRunner {
        private final List<String> eventSequence = new ArrayList<>();
        private final List<Long> timeStamps = new ArrayList<>();

        @Override
        public void executeEvent(long universeId, String eventId) {
            eventSequence.add(eventId);
            timeStamps.add(System.currentTimeMillis());
        }

        public List<String> getEventSequence() {
            return eventSequence;
        }

        public long getDuration() {
            if (timeStamps.size() > 1) {
                return timeStamps.get(timeStamps.size() - 1) - timeStamps.get(0);
            } else {
                return 0;
            }
        }

        public List<Long> getDelays() {
            List<Long> result = new ArrayList<>();
            for (int i = 0; i < timeStamps.size() - 1; i++) {
                result.add(timeStamps.get(i + 1) - timeStamps.get(i));
            }
            return result;
        }
    }

}