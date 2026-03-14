package FileManager.FileManager.ExceptionHandler;

import org.springframework.http.HttpStatus;

public class InsufficientCreditException extends ApiException{
    public InsufficientCreditException() {
        super("Insufficient credits", HttpStatus.PAYMENT_REQUIRED);
    }
}
