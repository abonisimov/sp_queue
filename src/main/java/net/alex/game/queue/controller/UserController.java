package net.alex.game.queue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.alex.game.queue.model.in.*;
import net.alex.game.queue.model.out.UserOut;
import net.alex.game.queue.service.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static net.alex.game.queue.config.security.AccessTokenService.AUTH_TOKEN_HEADER_NAME;

@RestController
@RequestMapping(path = "/v1/api/game")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Register new user account",
            tags = {"user"},
            method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserOut.class))
                    ),
                    @ApiResponse(responseCode = "401",
                            description = "Invalid registration data provided",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "409",
                            description = "Non unique email or nick name",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @PostMapping(value = "/users/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserOut register(@Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = UserPasswordIn.class)))
                            @RequestBody @Valid final UserPasswordIn userPasswordIn) {
        return userService.register(userPasswordIn);
    }

    @Operation(summary = "Sign in registered user",
            tags = {"user"},
            method = "POST",
            description = "Sign in registered user and return access token in \"" + AUTH_TOKEN_HEADER_NAME + "\" header",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserOut.class))
                    ),
                    @ApiResponse(responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied, account is blocked",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @PostMapping(value ="/users/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserOut login(@Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CredentialsIn.class)))
                         @RequestBody @Valid final CredentialsIn credentialsIn,
                         HttpServletResponse response) {
        return userService.login(credentialsIn, response);
    }

    @Operation(summary = "Reset to a password assigned by the user",
            tags = {"user"},
            method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied, account is blocked",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @PostMapping(value ="/users/resetpassword", produces = MediaType.APPLICATION_JSON_VALUE)
    public void resetPassword(@Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ResetPasswordIn.class)))
                              @RequestBody @Valid final ResetPasswordIn resetPasswordIn) {
        userService.resetPassword(resetPasswordIn);
    }

    @Operation(summary = "Switch the user status ENABLE|DISABLE",
            tags = {"user"},
            method = "PUT",
            security = @SecurityRequirement(name = "api_key", scopes = { "ADMIN" }),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
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
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping(value ="/users/{userId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public void changeUserStatus(@Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = UserStatusIn.class)))
                                 @RequestBody @Valid final UserStatusIn userStatusIn,
                                 @Parameter(description = "User id")
                                 @PathVariable(value = "userId") long userId) {
        userService.changeUserStatus(userId, userStatusIn);
    }

    @Operation(summary = "Delete user account",
            tags = {"user"},
            method = "DELETE",
            security = @SecurityRequirement(name = "api_key", scopes = { "ADMIN" }),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
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
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping(value ="/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteUser(@Parameter(description = "User id")
                           @PathVariable(value = "userId") long userId) {
        userService.deleteUser(userId);
    }

    @Operation(summary = "Retrieve user data",
            tags = {"user"},
            method = "GET",
            security = @SecurityRequirement(name = "api_key", scopes = { "ADMIN", "USER:SELF" }),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserOut.class))
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
                            description = "Specified user is not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @GetMapping(value ="/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("authentication.principal.userId == #userId or hasAuthority('ADMIN')")
    public UserOut getUser(@Parameter(description = "User id")
                           @PathVariable(value = "userId") long userId) {
        return userService.getUser(userId);
    }

    @Operation(summary = "Retrieve all users data",
            tags = {"user"},
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
                    )
            })
    @GetMapping(value ="/users", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<UserOut> getUsers(@SortDefault(sort = "lastLogin", direction = Sort.Direction.DESC)
                                  @PageableDefault @ParameterObject Pageable pageable) {
        return userService.getUsers(pageable);
    }

    @Operation(summary = "Change user data",
            tags = {"user"},
            method = "PUT",
            security = @SecurityRequirement(name = "api_key", scopes = { "USER:SELF" }),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserOut.class))
                    ),
                    @ApiResponse(responseCode = "401",
                            description = "Invalid user data provided",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied, account is blocked",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "409",
                            description = "Non unique nick name",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @PutMapping(value ="/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("authentication.principal.userId == #userId")
    public UserOut changeUser(@Parameter(description = "User id")
                              @PathVariable(value = "userId") long userId,
                              @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                      schema = @Schema(implementation = UserIn.class)))
                              @RequestBody @Valid final UserIn userIn) {
        return userService.changeUser(userId, userIn);
    }

    @Operation(summary = "Check token validness",
            tags = {"user"},
            method = "GET",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @GetMapping(value ="/users/istokenvalid/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean isTokenValid(@Parameter(description = "Token to validate")
                                @PathVariable(value = "token") String token,
                                HttpServletResponse response) {
        return userService.isTokenValid(token, response);
    }

    @Operation(summary = "Restore lost password request",
            tags = {"user"},
            method = "GET",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied, account is blocked",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "User with specified email is not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @GetMapping(value ="/users/restorepassword/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void restorePassword(@Parameter(description = "User email")
                                @PathVariable(value = "email") String email) {
        userService.restorePassword(email);
    }

    @Operation(summary = "Confirm lost password restore and reset password",
            tags = {"user"},
            method = "GET",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Success",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "401",
                            description = "Invalid credentials or expired confirmation token",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied, account is blocked",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "Specified confirmation token is not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @GetMapping(value ="/users/restorepassword/confirm/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void confirmRestorePassword(@Parameter(description = "Password restore confirmation token")
                                       @PathVariable(value = "token") String token,
                                       @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                               schema = @Schema(implementation = PasswordIn.class)))
                                       @RequestBody @Valid final PasswordIn passwordIn) {
        userService.confirmRestorePassword(token, passwordIn);
    }
}
