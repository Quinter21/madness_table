package com.quinter.madness_table.auth;

import com.quinter.madness_table.common.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyUsedException extends ApiException {

    public EmailAlreadyUsedException(String email){
        super(HttpStatus.CONFLICT, "El email ya está registrado: " + email);
    }
}
