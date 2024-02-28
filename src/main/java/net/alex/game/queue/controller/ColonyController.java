package net.alex.game.queue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import net.alex.game.model.Colony;
import net.alex.game.queue.QueueService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/v1/api/game")
public class ColonyController {

    private final QueueService queueService;

    public ColonyController(QueueService queueService) {
        this.queueService = queueService;
    }

    @Operation(summary = "Create new colony in a given universe",
            tags = {"colony"},
            method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Colony successfully created",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "No universe exists with a given id",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "409",
                            description = "Colony already exists",
                            content = @Content
                    )
            })
    @PostMapping(value = "/colonies")
    public void createColony(@Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                     schema = @Schema(implementation = Colony.class)))
                             @RequestBody @Valid Colony colony) {
        queueService.createColony(colony);
    }

    @Operation(summary = "Delete existing colony from a given universe",
            tags = {"colony"},
            method = "DELETE",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Colony successfully deleted",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "No colony exists with a given id",
                            content = @Content
                    )
            })
    @DeleteMapping(value = "/colonies/{colonyId}")
    public void deleteColony(@Parameter(description = "Colony id")
                             @PathVariable(value = "colonyId") String colonyId) {
        queueService.deleteColony(colonyId);
    }

    @Operation(summary = "Get colony by id",
            tags = {"colony"},
            method = "GET",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Colonies list successfully retrieved",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Colony.class))
                    )
            })
    @GetMapping(value = "/colonies/{colonyId}")
    public Colony getColony(@Parameter(description = "Colony id")
                            @PathVariable(value = "colonyId") String colonyId) {
        return queueService.getColony(colonyId);
    }
}
