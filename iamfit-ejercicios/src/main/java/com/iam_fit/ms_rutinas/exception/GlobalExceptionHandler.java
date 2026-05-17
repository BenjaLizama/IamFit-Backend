package com.iam_fit.ms_rutinas.exception;


import com.iam_fit.ms_rutinas.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ErrorResponseDto> manejarException(ChatException ex){
        ErrorResponseDto error = new ErrorResponseDto(
                    HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()

        );
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
    @ExceptionHandler(DocumentException.class)
    public ResponseEntity<ErrorResponseDto> excepcionDocumentos(DocumentException ex){
        ErrorResponseDto errorResponseDto =new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDto);
    }
}
