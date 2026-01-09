package FileManager.FileManager.ExceptionHandler;

import FileManager.FileManager.DTO.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHnadler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApiException(ApiException e  , HttpServletRequest request){
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .error(e.getHttpStatus().getReasonPhrase())
                .status(e.getHttpStatus().value())
                .timestamp(Instant.now())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(errorResponse.getStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnhandled(Exception e , HttpServletRequest request){
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .error("Internal Server Error")
                .status(500)
                .timestamp(Instant.now())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.internalServerError()
                .body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleForbidden(AccessDeniedException e){
        log.warn("Forbidden access : {}" , e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed");

    }
}
