package org.canopyplatform.canopy.downloadservice.exceptions.custom;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DownloadServiceReadWriteError extends RuntimeException {

    public DownloadServiceReadWriteError(String message){
        super(message);
    }

}
