package FileManager.FileManager.ExceptionHandler;

import org.springframework.http.HttpStatus;

public class SizeExcededException extends ApiException{
    public SizeExcededException() {
        super("File size limit exceeded", HttpStatus.PAYLOAD_TOO_LARGE);
    }
}
