package FileManager.FileManager.ExceptionHandler;

import org.springframework.http.HttpStatus;

public class FileNotFoundException extends ApiException{
    public FileNotFoundException() {
        super("File Not Found", HttpStatus.NOT_FOUND);
    }
}
