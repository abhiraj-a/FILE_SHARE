package FileManager.FileManager.ExceptionHandler;

import org.springframework.http.HttpStatus;

public class TransferNotFoundException extends ApiException{
    public TransferNotFoundException() {
        super("Transfer not found" , HttpStatus.NOT_FOUND);
    }
}
