package com.islamnizami.pdfwizarddevlab.exception;

import com.islamnizami.pdfwizarddevlab.model.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Couldn't find: {}", ex.getMessage());

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "A validation error occurred",
                request.getRequestURI(),
                errors
        );
    }

    @ExceptionHandler(PdfProcessingException.class)
    public ResponseEntity<ErrorResponseDTO> handlePdfProcessing(
            PdfProcessingException ex,
            HttpServletRequest request) {

        log.error("PDF processing error: ", ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleMaxSizeException(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "The uploaded file exceeds the allowed size limit (Max: 25MB)",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error: ", ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An internal server error occurred: " + ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    private ResponseEntity<ErrorResponseDTO> buildResponse(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> validationErrors) {

        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
