package plus.tson.exception;


public class TsonSyntaxException extends RuntimeException{
    public TsonSyntaxException(String msg, int cursor, char c){
        this(msg, cursor, "bad char: "+c);
    }


    public TsonSyntaxException(String msg, int cursor, Object c){
        super("Syntax exception: ["+c+"] in pos ["+cursor+"]:\n---ERROR START---\n"+msg+"\n----ERROR END----");
    }
}