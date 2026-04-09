package ex.org.project.downloadService.exceptions;

import ex.org.project.downloadService.auth.UserAuthenticationException;
import ex.org.project.downloadService.auth.UserAuthorizationException;
import ex.org.project.downloadService.auth.UserNotFoundException;
import ex.org.project.downloadService.exceptions.custom.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.concurrent.CompletionException;

@Slf4j
@RestController
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public final ResponseEntity<ExceptionResponseDTO> handleCompletionException(CompletionException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "AWS S3 Completion Exception",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler
    public final ResponseEntity<ExceptionResponseDTO> handleDownloadServiceReadWriteError(DownloadServiceReadWriteError e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Read/Write Error",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler
    public final ResponseEntity<ExceptionResponseDTO> handleDownloadServiceS3FileError(DownloadServiceS3FileError e) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "S3 File Not Found",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler
    public final ResponseEntity<ExceptionResponseDTO> handleDataFileNotFoundException(DataFileNotFoundException e){
        HttpStatus status = HttpStatus.NOT_FOUND;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Data File Not Found",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler
    public final ResponseEntity<ExceptionResponseDTO> handleDocumentFileNotFoundException(DocumentFileNotFoundException e){
        HttpStatus status = HttpStatus.NOT_FOUND;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Document Not Found",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler
    public final ResponseEntity<ExceptionResponseDTO> handleSubmissionNotFoundException(SubmissionNotFoundException e){
        HttpStatus status = HttpStatus.NOT_FOUND;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Submission Not Found",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(UserAuthorizationException.class)
    ResponseEntity<ExceptionResponseDTO> handleAuthorizationException(UserAuthorizationException e){
        HttpStatus status = HttpStatus.FORBIDDEN;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Unauthorized",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(UserAuthenticationException.class)
    ResponseEntity<ExceptionResponseDTO> handleAuthenticationException(UserAuthenticationException e){
        //why in the world does the "unauthorized" exception actually mean unauthenticated
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Unauthenticated",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<ExceptionResponseDTO> handleUserNotFoundException(UserNotFoundException e){
        HttpStatus status = HttpStatus.NOT_FOUND;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "User Not Found",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(StudyNotFoundException.class)
    ResponseEntity<ExceptionResponseDTO> handleStudyNotFoundException(StudyNotFoundException e){
        log.warn(e.getMessage());
        HttpStatus status = HttpStatus.NOT_FOUND;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Study Not Found",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(FileNotFoundException.class)
    ResponseEntity<ExceptionResponseDTO> handleFileNotFoundException(FileNotFoundException e){
        log.warn(e.getMessage());
        HttpStatus status = HttpStatus.NOT_FOUND;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "File Not Found",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ExceptionResponseDTO> handleBadRequestExceptionException(BadRequestException e){
        log.warn(e.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Bad Request",
                status.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<ExceptionResponseDTO> authenticationException(RuntimeException e){
        log.error(e.getMessage(), e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Internal Server Error",
                status.value(),
                "An unknown error has occurred. Please contact support if the issue persists."
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ExceptionResponseDTO> authenticationException(Exception e){
        log.error(e.getMessage(), e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Internal Server Error",
                status.value(),
                "An unknown error has occurred. Please contact support if the issue persists."
        );
        return new ResponseEntity<>(responseDTO, status);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public final ResponseEntity<ExceptionResponseDTO> methodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e){
        log.error(e.getMessage(), e);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionResponseDTO responseDTO = new ExceptionResponseDTO(
                "Invalid Id",
                status.value(),
                "Malformed Id found in the request."
        );
        return new ResponseEntity<>(responseDTO, status);
    }
}
