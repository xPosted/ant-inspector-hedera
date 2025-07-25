package com.antinspector.hedera.relations.web;

import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class WebExceptionHandler {

     @ExceptionHandler(Exception.class)
     public ResponseEntity<ErrorBody> handleStorageBusy(Exception ex) {
         return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorBody(ex.getMessage()));
     }

     @Value
     public static class ErrorBody {
         String message;

         public ErrorBody(String message) {
             this.message = message;
         }

     }

}
