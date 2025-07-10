package ex.org.project.downloadService.exceptions.custom;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String message) { super(message); }
}
