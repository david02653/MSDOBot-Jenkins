package soselab.david.msdobot.Exception;

/**
 * throw this exception if rest request goes wrong
 */
public class RequestFailException extends Exception{
    public RequestFailException(String errorMsg){
        super(errorMsg);
    }
    public RequestFailException(){
    }
}
