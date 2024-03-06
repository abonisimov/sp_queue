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
import net.alex.game.queue.service.UniverseService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/v1/api/game")
public class UniverseController {

    private final UniverseService universeService;

    public UniverseController(UniverseService universeService) {
        this.universeService = universeService;
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
    @GetMapping(value = "/universes/{universeId}/colonies", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Colony> getColoniesList(@Parameter(description = "Universe id")
                                        @PathVariable(value = "universeId") String universeId) {
        return universeService.getColoniesList(universeId);
    }

    @Operation(summary = "Create new universe",
            tags = {"universe"},
            method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Universe successfully created",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "409",
                            description = "Universe already exists",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @PostMapping(value = "/universes", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createUniverse(@Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Universe.class)))
                               @RequestBody @Valid Universe universe) {
        universeService.createUniverse(universe);
    }

    @Operation(summary = "Delete existing universe",
            tags = {"universe"},
            method = "DELETE",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Universe successfully deleted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "No universe exists with a given id",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @DeleteMapping(value = "/universes/{universeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteUniverse(@Parameter(description = "Universe id")
                             @PathVariable(value = "universeId") String universeId) {
        universeService.deleteUniverse(universeId);
    }

    @Operation(summary = "Start universe",
            tags = {"universe"},
            method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Universe successfully started",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "409",
                            description = "Universe was already running",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @PostMapping(value = "/universes/{universeId}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public void startUniverse(@Parameter(description = "Universe id")
                              @PathVariable(value = "universeId") String universeId) {
        universeService.startUniverse(universeId);
    }

    @Operation(summary = "Stop universe",
            tags = {"universe"},
            method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Universe successfully stopped",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "409",
                            description = "Universe was not running",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @PostMapping(value = "/universes/{universeId}/stop", produces = MediaType.APPLICATION_JSON_VALUE)
    public void stopUniverse(@Parameter(description = "Universe id")
                             @PathVariable(value = "universeId") String universeId) {
        universeService.stopUniverse(universeId);
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
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @GetMapping(value = "/universes/{universeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Universe getUniverse(@Parameter(description = "Universe id")
                                @PathVariable(value = "universeId") String universeId) {
        return universeService.getUniverse(universeId);
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
    @GetMapping(value = "/universes", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Universe> getUniversesList() {
        return universeService.getUniversesList();
    }
}
