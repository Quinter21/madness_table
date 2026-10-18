package com.quinter.madness_table.auth;

import com.quinter.madness_table.common.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException(){
        super(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
    }
}
