package br.edu.ifc.bikes.web.exception;

import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.exception.PasswordInvalidException;
import br.edu.ifc.bikes.exception.UsernameUniqueViolationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> methodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        return build(HttpStatus.UNPROCESSABLE_CONTENT, new ErrorMessage(
                request, HttpStatus.UNPROCESSABLE_CONTENT, "Campo(s) inválido(s)", ex.getBindingResult()));
    }

    @ExceptionHandler(UsernameUniqueViolationException.class)
    public ResponseEntity<ErrorMessage> usernameUniqueViolationException(
            UsernameUniqueViolationException ex, HttpServletRequest request) {

        return build(HttpStatus.CONFLICT, new ErrorMessage(request, HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorMessage> entityNotFoundException(
            EntityNotFoundException ex, HttpServletRequest request) {

        return build(HttpStatus.NOT_FOUND, new ErrorMessage(request, HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(PasswordInvalidException.class)
    public ResponseEntity<ErrorMessage> passwordInvalidException(
            PasswordInvalidException ex, HttpServletRequest request) {

        return build(HttpStatus.BAD_REQUEST, new ErrorMessage(request, HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    private ResponseEntity<ErrorMessage> build(HttpStatus status, ErrorMessage body) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }
}