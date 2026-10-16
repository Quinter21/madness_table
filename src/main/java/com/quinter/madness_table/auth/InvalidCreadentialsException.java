package com.quinter.madness_table.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCreadentialsException extends RuntimeException {

    public InvalidCreadentialsException(){
        super("Credenciales incorrectas");
    }
}
