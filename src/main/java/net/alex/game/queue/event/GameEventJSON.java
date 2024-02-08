package net.alex.game.queue.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class GameEventJSON {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GameEventJSON() {}

    public static GameEvent fromJSON(String wrapperJSON) throws IOException, ClassNotFoundException {
        JsonNode node = MAPPER.readTree(wrapperJSON);
        if (node.has("eventClass")) {
            String eventClassName = node.get("eventClass").asText();
            Class<?> eventClass = Class.forName(eventClassName);
            return (GameEvent) MAPPER.reader().readValue(node, eventClass);
        } else {
            throw new ClassNotFoundException("Can't find eventClass property in JSON");
        }
    }

    public static String toJSON(GameEvent gameEvent) throws JsonProcessingException {
        return MAPPER.writer().writeValueAsString(gameEvent);
    }
}
