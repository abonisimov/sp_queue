package net.alex.game.queue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.alex.game.queue.model.CredentialsIn;
import net.alex.game.queue.model.UserIn;
import net.alex.game.queue.model.UserOut;
import net.alex.game.queue.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                            description = "Registered user data",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = UserOut.class)))
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid registration data provided",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    )
            })
    @PostMapping(value = "/users/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserOut registerUserAccount(@Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = UserIn.class)))
                                       @RequestBody @Valid final UserIn userIn) {
        return userService.register(userIn);
    }

    @Operation(summary = "Sign in registered user",
            tags = {"user"},
            method = "POST",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "User data",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = UserOut.class)))
                    ),
                    @ApiResponse(responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(responseCode = "403",
                            description = "Access denied",
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
}
