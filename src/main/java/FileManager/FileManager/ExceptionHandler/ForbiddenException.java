package FileManager.FileManager.ExceptionHandler;

import org.springframework.http.HttpStatus;

public class ForbiddenException  extends  ApiException{
    public ForbiddenException() {
        super("Access denied", HttpStatus.FORBIDDEN);
    }
}
