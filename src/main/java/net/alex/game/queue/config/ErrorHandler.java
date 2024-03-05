package net.alex.game.queue.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.alex.game.queue.annotation.HttpStatusMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class ErrorHandler {

    private static final String MESSAGE = "message";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> onError(MethodArgumentNotValidException error) {
        Map<String, Object> errorBody = new HashMap<>();

        error.getBindingResult().getAllErrors().forEach(e -> {
            String name;
            if (e instanceof FieldError fieldError) {
                name = fieldError.getField();
            } else {
                name = e.getObjectName();
            }
            String errorMessage = e.getDefaultMessage();
            errorBody.put(name, errorMessage);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorBody);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> onError(MethodArgumentTypeMismatchException error) {
        Map<String, Object> errorBody = new HashMap<>();

        errorBody.put(MESSAGE, error.toString());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorBody);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> onError(AccessDeniedException error) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = !(authentication instanceof AnonymousAuthenticationToken);
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put(MESSAGE, isAuthenticated ? error.getMessage() : "Authentication is required");
        return ResponseEntity
                .status(isAuthenticated ? HttpStatus.FORBIDDEN.value() : HttpStatus.UNAUTHORIZED.value())
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorBody);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> onError(Throwable error, HttpServletRequest request) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put(MESSAGE, error.toString());

        if (error.getClass().isAnnotationPresent(HttpStatusMapping.class)) {
            return ResponseEntity
                    .status(error.getClass().getAnnotation(HttpStatusMapping.class).status())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorBody);
        } else {
            String uri = null;

            if (request != null) {
                uri = request.getRequestURI();
            }

            log.info("Can't do request '{}', cause {}: '{}'",
                    uri, error.getClass().getSimpleName(), error.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorBody);
        }
    }

}