package com.quinter.madness_table.campaign;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvalidInviteCodeException extends RuntimeException {

    public InvalidInviteCodeException() {

        super("Código de invitación no válido");
    }
}
