package FileManager.FileManager.ExceptionHandler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException{

    private final HttpStatus httpStatus;

    protected ApiException(String mss , HttpStatus httpStatus){
        super(mss);
        this.httpStatus=httpStatus;
    }

}
