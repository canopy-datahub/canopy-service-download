package org.canopyplatform.canopy.downloadservice.exceptions.custom;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DocumentFileNotFoundException extends RuntimeException {

    public DocumentFileNotFoundException(String message) {
        super(message);
    }

}
