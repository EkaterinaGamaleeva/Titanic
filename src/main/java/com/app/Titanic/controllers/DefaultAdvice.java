package com.app.Titanic.controllers;

import com.app.Titanic.response.PassengerErrorResponse;
import com.app.Titanic.response.PassengerNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice()
public class DefaultAdvice {
    @ExceptionHandler
    private ResponseEntity handlerException(PassengerNotFoundException e) {
        PassengerErrorResponse response = new PassengerErrorResponse("Человек с таким именем не найден", System.currentTimeMillis());

        return new ResponseEntity<>(response.getMassage(), HttpStatus.BAD_REQUEST);
    }

}
