package ex.org.project.downloadService.exceptions.custom;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
