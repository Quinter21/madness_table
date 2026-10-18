package com.quinter.madness_table.campaign;

public class NotGuardianException extends RuntimeException {
    public NotGuardianException() {

        super("Solo el guardián tiene el poder de acceder a este mundo");
    }
}
