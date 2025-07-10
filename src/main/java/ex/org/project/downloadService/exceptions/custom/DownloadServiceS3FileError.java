package ex.org.project.downloadService.exceptions.custom;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DownloadServiceS3FileError extends RuntimeException{

    public DownloadServiceS3FileError(String message){
        super(message);
    }

}
