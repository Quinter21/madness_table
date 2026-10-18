package com.quinter.madness_table.campaign;

import com.quinter.madness_table.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
