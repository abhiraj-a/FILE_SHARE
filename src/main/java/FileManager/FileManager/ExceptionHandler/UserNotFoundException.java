package FileManager.FileManager.ExceptionHandler;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException{
    public UserNotFoundException() {
        super("User Not Found", HttpStatus.NOT_FOUND);
    }
}
