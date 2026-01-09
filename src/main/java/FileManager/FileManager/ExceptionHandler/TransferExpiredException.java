package FileManager.FileManager.ExceptionHandler;

import org.springframework.http.HttpStatus;

public class TransferExpiredException extends ApiException{
    public TransferExpiredException() {
        super("Transfer Expired", HttpStatus.NOT_FOUND);
    }
}
