package net.alex.game.queue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import net.alex.game.model.Colony;
import net.alex.game.queue.service.ColonyService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/v1/api/game")
public class ColonyController {

    private final ColonyService colonyService;

    public ColonyController(ColonyService colonyService) {
        this.colonyService = colonyService;
    }

    @Operation(summary = "Create new colony in a given universe",
            tags = {"colony"},
            method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Colony successfully created",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "No universe exists with a given id",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "409",
                            description = "Colony already exists",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @PostMapping(value = "/colonies", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createColony(@Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                     schema = @Schema(implementation = Colony.class)))
                             @RequestBody @Valid Colony colony) {
        colonyService.createColony(colony);
    }

    @Operation(summary = "Delete existing colony from a given universe",
            tags = {"colony"},
            method = "DELETE",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Colony successfully deleted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "No colony exists with a given id",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @DeleteMapping(value = "/colonies/{colonyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteColony(@Parameter(description = "Colony id")
                             @PathVariable(value = "colonyId") String colonyId) {
        colonyService.deleteColony(colonyId);
    }

    @Operation(summary = "Get colony by id",
            tags = {"colony"},
            method = "GET",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Colony successfully retrieved",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Colony.class))
                    )
            })
    @GetMapping(value = "/colonies/{colonyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Colony getColony(@Parameter(description = "Colony id")
                            @PathVariable(value = "colonyId") String colonyId) {
        return colonyService.getColony(colonyId);
    }
}
