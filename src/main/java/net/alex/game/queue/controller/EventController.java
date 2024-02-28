package net.alex.game.queue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import net.alex.game.model.event.GameEvent;
import net.alex.game.queue.QueueService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1/api/game")
public class EventController {

    private final QueueService queueService;

    public EventController(QueueService queueService) {
        this.queueService = queueService;
    }

    @Operation(summary = "Add event to queue for given colony in the given universe",
            tags = {"event"},
            method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Event successfully added",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "No universe or colony exists with a given id",
                            content = @Content
                    )
            })
    @PostMapping(value = "/events")
    public void addEvent(@Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                 schema = @Schema(implementation = GameEvent.class)))
                         @RequestBody @Valid GameEvent event) {
        queueService.addEvent(event);
    }
}
