package com.quinter.madness_table.campaign;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class JoinRequestConflictException extends RuntimeException {

    public JoinRequestConflictException(String message) {
        super(message);
    }
}
