package net.alex.game.queue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import net.alex.game.model.Colony;
import net.alex.game.model.Universe;
import net.alex.game.queue.QueueService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/v1/api/game")
public class UniverseController {

    private final QueueService queueService;

    public UniverseController(QueueService queueService) {
        this.queueService = queueService;
    }

    @Operation(summary = "Get colonies list for given universe",
            tags = {"universe"},
            method = "GET",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Colonies list successfully retrieved",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = Colony.class)))
                    )
            })
    @GetMapping(value = "/universes/{universeId}/colonies")
    public List<Colony> getColoniesList(@Parameter(description = "Universe id")
                                        @PathVariable(value = "universeId") String universeId) {
        return queueService.getColoniesList(universeId);
    }

    @Operation(summary = "Create new universe",
            tags = {"universe"},
            method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Universe successfully created",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "409",
                            description = "Universe already exists",
                            content = @Content
                    )
            })
    @PostMapping(value = "/universes")
    public void createUniverse(@Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Universe.class)))
                               @RequestBody @Valid Universe universe) {
        queueService.createUniverse(universe);
    }

    @Operation(summary = "Delete existing universe",
            tags = {"universe"},
            method = "DELETE",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Universe successfully deleted",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "No universe exists with a given id",
                            content = @Content
                    )
            })
    @DeleteMapping(value = "/universes/{universeId}")
    public void deleteColony(@Parameter(description = "Universe id")
                             @PathVariable(value = "universeId") String universeId) {
        queueService.deleteUniverse(universeId);
    }

    @Operation(summary = "Start universe",
            tags = {"universe"},
            method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Universe successfully started",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "409",
                            description = "Universe was already running",
                            content = @Content
                    )
            })
    @PostMapping(value = "/universes/{universeId}/start")
    public void startUniverse(@Parameter(description = "Universe id")
                              @PathVariable(value = "universeId") String universeId) {
        queueService.startUniverse(universeId);
    }

    @Operation(summary = "Stop universe",
            tags = {"universe"},
            method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Universe successfully stopped",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "409",
                            description = "Universe was not running",
                            content = @Content
                    )
            })
    @PostMapping(value = "/universes/{universeId}/stop")
    public void stopUniverse(@Parameter(description = "Universe id")
                             @PathVariable(value = "universeId") String universeId) {
        queueService.stopUniverse(universeId);
    }

    @Operation(summary = "Get universe",
            tags = {"universe"},
            method = "GET",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Universe successfully retrieved",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Universe.class))
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "No universe exists with a given id",
                            content = @Content
                    )
            })
    @GetMapping(value = "/universes/{universeId}")
    public Universe getUniverse(@Parameter(description = "Universe id")
                                @PathVariable(value = "universeId") String universeId) {
        return queueService.getUniverse(universeId);
    }

    @Operation(summary = "Get all registered universes list",
            tags = {"universe"},
            method = "GET",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Universes list successfully retrieved",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = Universe.class)))
                    )
            })
    @GetMapping(value = "/universes")
    public List<Universe> getUniversesList() {
        return queueService.getUniversesList();
    }
}
