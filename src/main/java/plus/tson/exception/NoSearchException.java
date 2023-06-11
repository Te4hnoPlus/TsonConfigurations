package plus.tson.exception;


public class NoSearchException extends RuntimeException {
    public NoSearchException(Object o){
        super("Value -> "+o.toString());
    }
    public NoSearchException(){}
}