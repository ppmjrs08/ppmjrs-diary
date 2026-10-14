package edu.smartcampus.m2;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.slf4j.*;

@RestControllerAdvice
public class ErrorHandler {
    private static final Logger log=LoggerFactory.getLogger(ErrorHandler.class);
    @ExceptionHandler({IllegalArgumentException.class,MethodArgumentNotValidException.class,HttpMessageNotReadableException.class})
    public ResponseEntity<?> input(Exception e){return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_INPUT","Check interests (1-500 chars), goals (0-300 chars), topK (1-3), and allowed fields"));}
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> status(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(ApiResponse.error("REQUEST_REJECTED",e.getReason()));}
    @ExceptionHandler({org.springframework.web.servlet.resource.NoResourceFoundException.class,org.springframework.web.servlet.NoHandlerFoundException.class})
    public ResponseEntity<?> missing(Exception e){return ResponseEntity.status(404).body(ApiResponse.error("NOT_FOUND","Resource not found"));}
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unexpected(Exception e){log.error("M2 request failed",e);return ResponseEntity.internalServerError().body(ApiResponse.error("SERVER_ERROR","Unable to process the request. Please try again later."));}
}
