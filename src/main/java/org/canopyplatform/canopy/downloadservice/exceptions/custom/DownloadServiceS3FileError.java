package org.canopyplatform.canopy.downloadservice.exceptions.custom;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DownloadServiceS3FileError extends RuntimeException{

    public DownloadServiceS3FileError(String message){
        super(message);
    }

}
