package tn.enis.DemandeConge.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice

public class GlobalExceptionHandler {



    @ExceptionHandler(IllegalArgumentException.class)

    public ResponseEntity<String> handleBusinessLogic(IllegalArgumentException ex) {

        // Renvoie le message d'erreur au front-end avec un code 400

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());

    }

}