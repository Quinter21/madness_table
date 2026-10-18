package com.quinter.madness_table.campaign;

import com.quinter.madness_table.common.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvalidInviteCodeException extends ApiException {

    public InvalidInviteCodeException() {

        super(HttpStatus.NOT_FOUND, "Código de invitación no válido");
    }
}
