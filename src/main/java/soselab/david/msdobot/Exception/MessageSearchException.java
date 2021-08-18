package soselab.david.msdobot.Exception;

/**
 * if anything goes wrong while handling message
 * for example, no matched additional question
 * no matched rasa intent
 */
public class MessageSearchException extends Exception {
    public MessageSearchException(String errorMsg){
        super(errorMsg);
    }
    public MessageSearchException(){
        super();
    }
}
