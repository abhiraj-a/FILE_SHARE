package FileManager.FileManager.ExceptionHandler;

import org.springframework.http.HttpStatus;

public class TransferRevokedException extends ApiException{
    public TransferRevokedException() {
        super("Transfer Revoked", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
