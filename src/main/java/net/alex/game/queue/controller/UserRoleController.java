package net.alex.game.queue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import net.alex.game.queue.model.in.RoleIn;
import net.alex.game.queue.service.UserRoleService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping(path = "/v1/api/game")
public class UserRoleController {

    private final UserRoleService userRoleService;

    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @Operation(summary = "Assign roles to user",
            tags = {"user", "role"},
            method = "PUT",
            security = @SecurityRequirement(name = "api_key", scopes = { "ALL" }),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid or empty incoming roles list",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied, account is blocked or action is restricted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "Specified user is not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @PutMapping(value ="/users/{userId}/roles/assign", produces = MediaType.APPLICATION_JSON_VALUE)
    public void assignRoles(@Parameter(description = "User id")
                            @PathVariable(value = "userId") long userId,
                            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = RoleIn.class))))
                            @RequestBody @NotEmpty final List<@Valid RoleIn> roles) {
        userRoleService.assignRoles(userId, roles);
    }

    @Operation(summary = "Unassign roles from user",
            tags = {"user", "role"},
            method = "DELETE",
            security = @SecurityRequirement(name = "api_key", scopes = { "ALL" }),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid or empty incoming roles list",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied, account is blocked or action is restricted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "Specified user is not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @DeleteMapping(value ="/users/{userId}/roles/unassign", produces = MediaType.APPLICATION_JSON_VALUE)
    public void unassignRoles(@Parameter(description = "User id")
                              @PathVariable(value = "userId") long userId,
                              @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                      array = @ArraySchema(schema = @Schema(implementation = RoleIn.class))))
                              @RequestBody @NotEmpty final List<@Valid RoleIn> roles) {
        userRoleService.unassignRoles(userId, roles);
    }

    @Operation(summary = "The list of roles this account is able to assign for given user",
            tags = {"user", "role"},
            method = "GET",
            security = @SecurityRequirement(name = "api_key", scopes = { "ALL" }),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = RoleIn.class)))
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid or empty incoming roles list",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied, account is blocked or action is restricted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "Specified user is not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @GetMapping(value ="/users/{userId}/roles/assign/candidates", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<RoleIn> assignRoleCandidates(@Parameter(description = "User id")
                                             @PathVariable(value = "userId") long userId,
                                             @SortDefault(sort = "rank", direction = Sort.Direction.DESC)
                                             @PageableDefault @ParameterObject Pageable pageable) {
        return userRoleService.assignRolesCandidates(userId, pageable);
    }

    @Operation(summary = "The list of roles this account is able to unassign for given user",
            tags = {"user", "role"},
            method = "GET",
            security = @SecurityRequirement(name = "api_key", scopes = { "ALL" }),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = RoleIn.class)))
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid or empty incoming roles list",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied, account is blocked or action is restricted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "Specified user is not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @GetMapping(value ="/users/{userId}/roles/unassign/candidates", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<RoleIn> unassignRoleCandidates(@Parameter(description = "User id")
                                               @PathVariable(value = "userId") long userId,
                                               @SortDefault(sort = "rank", direction = Sort.Direction.DESC)
                                               @PageableDefault @ParameterObject Pageable pageable) {
        return userRoleService.unassignRolesCandidates(userId, pageable);
    }

    @Operation(summary = "Request ROOT access in empty system if there is only one user with one USER role",
            tags = {"user", "role"},
            method = "GET",
            security = @SecurityRequirement(name = "api_key", scopes = { "ALL" }),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = RoleIn.class)))
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid or empty incoming roles list",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied, account is blocked or action is restricted",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping(value ="/users/roles/request_root", produces = MediaType.APPLICATION_JSON_VALUE)
    public void requestRoot() {
        userRoleService.requestRoot();
    }
}
