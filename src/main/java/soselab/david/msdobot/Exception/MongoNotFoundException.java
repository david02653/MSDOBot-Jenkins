package soselab.david.msdobot.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * throw this exception if query data not found in mongodb
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class MongoNotFoundException extends RuntimeException{

    public MongoNotFoundException(){
        super();
    }

    public MongoNotFoundException(String errorMsg){
        super(errorMsg);
    }
}
