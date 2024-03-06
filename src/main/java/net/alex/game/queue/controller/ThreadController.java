package net.alex.game.queue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import net.alex.game.queue.service.ThreadService;
import net.alex.game.queue.thread.GameThreadStats;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/v1/api/game")
public class ThreadController {

    private final ThreadService threadService;

    public ThreadController(ThreadService threadService) {
        this.threadService = threadService;
    }

    @Operation(summary = "Get current statistics for particular thread",
            tags = {"thread"},
            method = "GET",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Thread statistics successfully retrieved",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = GameThreadStats.class))
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "No thread exists with a given id",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @GetMapping(value = "/threads/{threadId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public GameThreadStats getThreadStatistics(@Parameter(description = "Thread id")
                                               @PathVariable(value = "threadId") long threadId) {
        return threadService.getThreadStatistics(threadId);
    }

    @Operation(summary = "Get current statistics",
            tags = {"thread"},
            method = "GET",
            security = @SecurityRequirement(name = "api_key", scopes = { "ADMIN" }),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Thread statistics list successfully retrieved",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = GameThreadStats.class)))
                    ),
                    @ApiResponse(responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied, account is blocked or data is restricted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @GetMapping(value = "/threads", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<GameThreadStats> getThreadStatisticsList() {
        return threadService.getThreadStatisticsList();
    }
}
