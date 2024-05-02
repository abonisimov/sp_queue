package net.alex.game.queue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import net.alex.game.queue.model.out.RoleOut;
import net.alex.game.queue.model.out.UserOut;
import net.alex.game.queue.service.RoleService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1/api/game")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "Get roles list",
            tags = {"role"},
            method = "GET",
            security = @SecurityRequirement(name = "api_key", scopes = { "ADMIN" }),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = RoleOut.class)))
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
    @GetMapping(value ="/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<RoleOut> getRoles(@SortDefault(sort = "rank", direction = Sort.Direction.DESC)
                                  @PageableDefault @ParameterObject Pageable pageable) {
        return roleService.getRoles(pageable);
    }

    @Operation(summary = "Retrieve user's list by role",
            tags = {"role"},
            method = "GET",
            security = @SecurityRequirement(name = "api_key", scopes = { "ADMIN" }),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = UserOut.class)))
                    ),
                    @ApiResponse(responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied, account is blocked or data is restricted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "Specified role not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @GetMapping(value ="/roles/{roleId}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<UserOut> getUsers(@Parameter(description = "Role id")
                                  @PathVariable(value = "roleId") long roleId,
                                  @SortDefault(sort = "name", direction = Sort.Direction.DESC)
                                  @PageableDefault @ParameterObject Pageable pageable) {
        return roleService.getUsers(roleId, pageable);
    }
}
