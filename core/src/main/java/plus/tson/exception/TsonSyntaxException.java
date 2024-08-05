package plus.tson.exception;


/**
 * Exception for Tson syntax, called parsers if something is wrong
 * Can be created with TsonSyntaxException.make(..)
 */
public class TsonSyntaxException extends RuntimeException {
    private TsonSyntaxException(String msg, int cursor, Object c) {
        super("Syntax exception: [" + c + "] in pos [" + cursor + "]:\n---ERROR START---\n" + msg + "\n----ERROR END----");
    }


    private TsonSyntaxException(String msg, int cursor, Object c, Throwable err) {
        super("Syntax exception: [" + c + "] in pos [" + cursor + "]:\n---ERROR START---\n" + msg + "\n----ERROR END----", err);
    }


    private TsonSyntaxException(String msg, int line, int cursor, Object c) {
        super("Syntax exception: [" + c + "] in line ["+line+"] pos [" + cursor + "]:\n---ERROR START---\n" + msg + "\n----ERROR END----");
    }


    private TsonSyntaxException(String msg, int line, int cursor, Object c, Throwable err) {
        super("Syntax exception: [" + c + "] in line ["+line+"] pos [" + cursor + "]:\n---ERROR START---\n" + msg + "\n----ERROR END----", err);
    }


    /**
     * Remove 2 last (local) elements from stack trace
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        super.fillInStackTrace();
        StackTraceElement[] pre = super.getStackTrace();
        StackTraceElement[] result = new StackTraceElement[pre.length-2];
        System.arraycopy(pre, 2, result, 0, result.length);
        setStackTrace(result);
        return this;
    }


    /**
     * @param cursor The assumed position of the error
     * @param data Tson data
     */
    public static TsonSyntaxException make(int cursor, byte[] data){
        return make0(cursor, data, data[cursor]);
    }


    /**
     * @param cursor The assumed position of the error
     * @param data Tson data
     * @param msg Error message
     */
    public static TsonSyntaxException make(int cursor, char[] data, Object msg){
        return make0(cursor, data, msg);
    }


    /**
     * @param cursor The assumed position of the error
     * @param data Tson data
     */
    public static TsonSyntaxException make(int cursor, char[] data){
        return make0(cursor, data, data[cursor]);
    }


    /**
     * @param cursor The assumed position of the error
     * @param data Tson data
     * @param msg Error message
     */
    public static TsonSyntaxException make(int cursor, byte[] data, Object msg){
        return make0(cursor, data, msg);
    }


    /**
     * Build TsonSyntaxException with detail error message
     */
    private static TsonSyntaxException make0(int cursor, char[] data, Object msg){
        if(msg instanceof Character){
            msg = "bad char: " + msg;
        }
        int line = countLinesIn(cursor, data);
        String err = getErrorString(cursor, data);
        if(line < 2)return new TsonSyntaxException(err, cursor, msg);
        return new TsonSyntaxException(err, line, cursor, msg);
    }


    /**
     * Build TsonSyntaxException with detail error message
     */
    private static TsonSyntaxException make0(int cursor, byte[] data, Object msg){
        Throwable cause;
        if(msg instanceof Throwable){
            cause = (Throwable) msg;
            msg = cause.getMessage();
            if(msg == null)msg = " ? ";
        } else {
            cause = null;
            if(msg instanceof Character){
                msg = "bad char: " + msg;
            }
        }
        int line = countLinesIn(cursor, data);
        String err = getErrorString(cursor, data);
        if(line < 2){
            if(cause != null)return new TsonSyntaxException(err, cursor, msg, cause);
            return new TsonSyntaxException(err, cursor, msg);
        }else{
            if(cause != null)return new TsonSyntaxException(err, line, cursor, msg, cause);
            return new TsonSyntaxException(err, line, cursor, msg);
        }
    }


    /**
     * Gets the string that probably contains the error
     */
    public static String getErrorString(int cursor, byte[] data) {
        int min = Math.max(0, cursor - 50);
        if(min != 0){
            while (min != 0){
                if(data[min] == '\n'){
                    min += 1;
                    break;
                }
                --min;
            }
        }
        int max = Math.min(cursor + 50, data.length - 1);
        byte[] bytes = new byte[max - min];
        System.arraycopy(data, min, bytes, 0, bytes.length);
        return new String(bytes);
    }


    /**
     * Calculate count lines before error
     */
    private static int countLinesIn(int cursor, byte[] data){
        int lines = 0;
        while (cursor > 0){
            if(data[cursor] == '\n')++lines;
            --cursor;
        }
        return lines;
    }


    /**
     * Gets the string that probably contains the error
     */
    public static String getErrorString(int cursor, char[] data) {
        int min = Math.max(0, cursor - 50);
        if(min != 0){
            int prev = min;
            int lim = 100;
            while (min != 0){
                if(--lim < 0){
                    min = prev;
                    break;
                }
                if(data[min] == '\n'){
                    min += 1;
                    break;
                }
                --min;
            }
        }
        int max = Math.min(cursor + 70, data.length - 1);
        for (int i = cursor; i < max; i++){
            if(data[i] == '\n'){
                max = i-1;
                break;
            }
        }
        char[] chars = new char[max - min];
        System.arraycopy(data, min, chars, 0, chars.length);
        return new String(chars);
    }


    /**
     * Calculate count lines before error
     */
    private static int countLinesIn(int cursor, char[] data){
        int lines = 0;
        while (cursor > 0){
            if(data[cursor] == '\n')++lines;
            --cursor;
        }
        return lines;
    }
}