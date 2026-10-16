package com.quinter.madness_table.campaign;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CampaignNotFoundException extends RuntimeException{

    public CampaignNotFoundException(Long id){
        super("Campaña no encontrada " + id);
    }
}
