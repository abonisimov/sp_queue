package net.alex.game.queue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import net.alex.game.model.ColonyDescription;
import net.alex.game.model.Universe;
import net.alex.game.model.UniverseDescription;
import net.alex.game.queue.service.UniverseService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
            security = @SecurityRequirement(name = "api_key", scopes =
                    { "ADMIN", "OWNER:UNIVERSE", "MEMBER:UNIVERSE", "WATCHER:UNIVERSE" }),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Colonies list successfully retrieved",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = ColonyDescription.class)))
                    )
            })
    @PreAuthorize("hasAuthority('ADMIN') or " +
            "hasAuthority('OWNER:Universe:' + #universeId) or " +
            "hasAuthority('MEMBER:Universe:' + #universeId) or " +
            "hasAuthority('WATCHER:Universe:' + #universeId)")
    @GetMapping(value = "/universes/{universeId}/colonies", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ColonyDescription> getColoniesList(@Parameter(description = "Universe id")
                                                   @PathVariable(value = "universeId") String universeId,
                                                   @SortDefault(sort = "id", direction = Sort.Direction.DESC)
                                                   @PageableDefault @ParameterObject Pageable pageable) {
        return universeService.getColoniesList(universeId, pageable);
    }

    @Operation(summary = "Create new universe",
            tags = {"universe"},
            method = "POST",
            security = @SecurityRequirement(name = "api_key", scopes = { "USER" }),
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
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping(value = "/universes", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createUniverse(@Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Universe.class)))
                               @RequestBody @Valid Universe universe) {
        universeService.createUniverse(universe);
    }

    @Operation(summary = "Delete existing universe",
            tags = {"universe"},
            method = "DELETE",
            security = @SecurityRequirement(name = "api_key", scopes = { "ADMIN", "OWNER:UNIVERSE" }),
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
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('OWNER:Universe:' + #universeId)")
    @DeleteMapping(value = "/universes/{universeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteUniverse(@Parameter(description = "Universe id")
                               @PathVariable(value = "universeId") String universeId) {
        universeService.deleteUniverse(universeId);
    }

    @Operation(summary = "Start universe",
            tags = {"universe"},
            method = "POST",
            security = @SecurityRequirement(name = "api_key", scopes = { "ADMIN", "OWNER:UNIVERSE" }),
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
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('OWNER:Universe:' + #universeId)")
    @PostMapping(value = "/universes/{universeId}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public void startUniverse(@Parameter(description = "Universe id")
                              @PathVariable(value = "universeId") String universeId) {
        universeService.startUniverse(universeId);
    }

    @Operation(summary = "Stop universe",
            tags = {"universe"},
            method = "POST",
            security = @SecurityRequirement(name = "api_key", scopes = { "ADMIN", "OWNER:UNIVERSE" }),
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
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('OWNER:Universe:' + #universeId)")
    @PostMapping(value = "/universes/{universeId}/stop", produces = MediaType.APPLICATION_JSON_VALUE)
    public void stopUniverse(@Parameter(description = "Universe id")
                             @PathVariable(value = "universeId") String universeId) {
        universeService.stopUniverse(universeId);
    }

    @Operation(summary = "Get universe",
            tags = {"universe"},
            method = "GET",
            security = @SecurityRequirement(name = "api_key", scopes =
                    { "ADMIN", "OWNER:UNIVERSE", "MEMBER:UNIVERSE", "WATCHER:UNIVERSE" }),
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
    @PreAuthorize("hasAuthority('ADMIN') or " +
            "hasAuthority('OWNER:Universe:' + #universeId) or " +
            "hasAuthority('MEMBER:Universe:' + #universeId) or " +
            "hasAuthority('WATCHER:Universe:' + #universeId)")
    @GetMapping(value = "/universes/{universeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Universe getUniverse(@Parameter(description = "Universe id")
                                @PathVariable(value = "universeId") String universeId) {
        return universeService.getUniverse(universeId);
    }

    @Operation(summary = "Get all registered universes list",
            tags = {"universe"},
            method = "GET",
            security = @SecurityRequirement(name = "api_key", scopes = { "USER" }),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Universes list successfully retrieved",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = UniverseDescription.class)))
                    )
            })
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping(value = "/universes", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<UniverseDescription> getUniversesList(@SortDefault(sort = "id", direction = Sort.Direction.DESC)
                                                      @PageableDefault @ParameterObject Pageable pageable) {
        return universeService.getUniversesList(pageable);
    }
}
