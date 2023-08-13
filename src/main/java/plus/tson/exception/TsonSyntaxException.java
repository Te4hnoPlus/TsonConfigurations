package plus.tson.exception;


public class TsonSyntaxException extends RuntimeException{
    public TsonSyntaxException(String msg, int cursor, char c){
        super("Syntax exception at char ["+c+"] in pos ["+cursor+"]:\n---ERROR START---\n"+msg+"\n----ERROR END----");
    }
}