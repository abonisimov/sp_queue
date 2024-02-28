package net.alex.game.queue.thread;

import lombok.extern.slf4j.Slf4j;
import net.alex.game.model.event.GameEvent;
import net.alex.game.queue.event.FastModeSwitchEvent;
import net.alex.game.queue.event.QueueTerminationEvent;
import net.alex.game.queue.executor.EventExecutor;
import net.alex.game.queue.serialize.EventSerializer;
import net.alex.game.queue.serialize.InMemoryEventSerializer;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class GameEventThreadTest {
    static Map<Long, String> eventIdToDuration = new LinkedHashMap<>();
    static {
        eventIdToDuration.put(100L, "1");
        eventIdToDuration.put(0L, "2");
        eventIdToDuration.put(400L, "3");
        eventIdToDuration.put(300L, "4");
    }

    static Map<Long, String> eventIdToDurationSeconds = new LinkedHashMap<>();
    static {
        eventIdToDurationSeconds.put(1L, "1");
        eventIdToDurationSeconds.put(0L, "2");
        eventIdToDurationSeconds.put(3L, "3");
        eventIdToDurationSeconds.put(2L, "4");
    }

    @Test
    void testQueueInStandardMode() throws InterruptedException {
        long maxDuration = eventIdToDuration.keySet().stream().max(Long::compareTo).orElseThrow();
        long waitTime = maxDuration + 500;

        TestEventExecutor eventRunner = new TestEventExecutor();
        runEventThread(eventRunner,
                eventIdToDuration.entrySet().stream().
                        map(e -> GameEvent.builder().universeId("1").id(e.getValue()).delay(e.getKey()).timeUnit(TimeUnit.MILLISECONDS).build()).
                        collect(Collectors.toList()),
                waitTime);

        checkEventsSequence(eventRunner, eventIdToDuration);
        assertTrue(eventRunner.getDuration() >= maxDuration);
        assertTrue(eventRunner.getDuration() <= waitTime);
    }

    @Test
    void testQueueInStandardModeSeconds() throws InterruptedException {
        long maxDuration = eventIdToDurationSeconds.keySet().stream().max(Long::compareTo).orElseThrow();
        long waitTime = maxDuration * 1000 + 500;

        TestEventExecutor eventRunner = new TestEventExecutor();
        runEventThread(eventRunner,
                eventIdToDurationSeconds.entrySet().stream().
                        map(e -> GameEvent.builder().universeId("1").id(e.getValue()).delay(e.getKey()).timeUnit(TimeUnit.SECONDS).build()).
                        collect(Collectors.toList()),
                waitTime);

        checkEventsSequence(eventRunner, eventIdToDurationSeconds);
        assertTrue(eventRunner.getDuration() >= maxDuration);
        assertTrue(eventRunner.getDuration() <= waitTime);
    }

    @Test
    void testQueueInFastMode() throws InterruptedException {
        long maxDuration = eventIdToDuration.keySet().stream().max(Long::compareTo).orElseThrow();
        long waitTime = maxDuration + 500;

        TestEventExecutor eventRunner = new TestEventExecutor();
        List<GameEvent> events = new ArrayList<>();
        events.add(FastModeSwitchEvent.builder().universeId("1").id("f1").delay(0).timeUnit(TimeUnit.MILLISECONDS).enable(true).build());
        events.addAll(eventIdToDuration.entrySet().stream().
                map(e -> GameEvent.builder().universeId("1").id(e.getValue()).delay(e.getKey()).timeUnit(TimeUnit.MILLISECONDS).build()).toList());
        runEventThread(eventRunner, events, waitTime);

        checkEventsSequence(eventRunner, eventIdToDuration);
        assertTrue(eventRunner.getDuration() < maxDuration);
    }

    @Test
    void testQueueInMixedMode() throws InterruptedException {
        long maxDuration = eventIdToDuration.keySet().stream().max(Long::compareTo).orElseThrow();
        long waitTime = maxDuration + 500;

        TestEventExecutor eventRunner = new TestEventExecutor();
        List<GameEvent> events = new ArrayList<>();
        events.add(FastModeSwitchEvent.builder().universeId("1").id("f1").delay(150).timeUnit(TimeUnit.MILLISECONDS).enable(true).build());
        events.add(FastModeSwitchEvent.builder().universeId("1").id("f0").delay(350).timeUnit(TimeUnit.MILLISECONDS).enable(false).build());
        events.addAll(eventIdToDuration.entrySet().stream().
                map(e -> GameEvent.builder().universeId("1").id(e.getValue()).delay(e.getKey()).timeUnit(TimeUnit.MILLISECONDS).build()).toList());
        runEventThread(eventRunner, events, waitTime);

        checkEventsSequence(eventRunner, eventIdToDuration);
        assertTrue(eventRunner.getDuration() < maxDuration);
    }

    private void runEventThread(EventExecutor eventExecutor,
                                List<GameEvent> events,
                                long waitTime) throws InterruptedException {
        GameEventThread thread = new GameEventThread(eventExecutor, 10);
        new Thread(thread).start();
        events.forEach(thread::addEvent);
        Thread.sleep(waitTime);
        thread.addEvent(QueueTerminationEvent.
                builder().
                universeId("1").
                id(UUID.randomUUID().toString()).
                delay(0).
                timeUnit(TimeUnit.MILLISECONDS).
                shutdownLatch(new CountDownLatch(1)).
                eventSerializer(new InMemoryEventSerializer()).
                build());
    }

    private void checkEventsSequence(TestEventExecutor eventRunner, Map<Long, String> eventToDurationMap) {
        List<String> expectedEventSequence = eventToDurationMap.entrySet().stream().
                sorted(Comparator.comparingLong(Map.Entry::getKey)).map(Map.Entry::getValue).toList();
        assertEquals(expectedEventSequence, eventRunner.getEventSequence());
    }

    static class TestEventExecutor implements EventExecutor {
        private final List<String> eventSequence = new ArrayList<>();
        private final List<Long> timeStamps = new ArrayList<>();

        @Override
        public boolean executeEvent(GameEvent gameEvent) {
            eventSequence.add(gameEvent.getId());
            timeStamps.add(System.currentTimeMillis());
            return true;
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

    private static class DisabledEventSerializer extends EventSerializer {
        public List<String> readFromDataStore() {
            return Collections.emptyList();
        }
        public void writeToDataStore(List<String> events) {}
    }
}