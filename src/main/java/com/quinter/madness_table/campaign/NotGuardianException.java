package com.quinter.madness_table.campaign;

import com.quinter.madness_table.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class NotGuardianException extends ApiException {
    public NotGuardianException() {
        super(HttpStatus.FORBIDDEN, "Solo el guardián tiene el poder de acceder a este mundo");
    }
}
