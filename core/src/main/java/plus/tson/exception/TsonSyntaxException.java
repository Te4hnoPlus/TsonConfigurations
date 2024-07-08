package plus.tson.exception;


public class TsonSyntaxException extends RuntimeException {
    private TsonSyntaxException(String msg, int cursor, Object c) {
        super("Syntax exception: [" + c + "] in pos [" + cursor + "]:\n---ERROR START---\n" + msg + "\n----ERROR END----");
    }


    private TsonSyntaxException(String msg, int line, int cursor, Object c) {
        super("Syntax exception: [" + c + "] in line ["+line+"] pos [" + cursor + "]:\n---ERROR START---\n" + msg + "\n----ERROR END----");
    }


    @Override
    public synchronized Throwable fillInStackTrace() {
        super.fillInStackTrace();
        StackTraceElement[] pre = super.getStackTrace();
        StackTraceElement[] result = new StackTraceElement[pre.length-1];
        System.arraycopy(pre, 1, result, 0, result.length);
        setStackTrace(result);
        return this;
    }


    public static TsonSyntaxException make(int cursor, byte[] data){
        return make(cursor, data, data[cursor]);
    }


    public static TsonSyntaxException make(int cursor, char[] data, Object msg){
        if(msg instanceof Character){
            msg = "bad char: " + msg;
        }
        int line = countLinesIn(cursor, data);
        String err = getErrorString(cursor, data);
        if(line < 2)return new TsonSyntaxException(err, cursor, msg);
        return new TsonSyntaxException(err, line, cursor, msg);
    }


    public static TsonSyntaxException make(int cursor, char[] data){
        return make(cursor, data, data[cursor]);
    }


    public static TsonSyntaxException make(int cursor, byte[] data, Object msg){
        if(msg instanceof Character){
            msg = "bad char: " + msg;
        }
        int line = countLinesIn(cursor, data);
        String err = getErrorString(cursor, data);
        if(line < 2)return new TsonSyntaxException(err, cursor, msg);
        return new TsonSyntaxException(err, line, cursor, msg);
    }


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


    private static int countLinesIn(int cursor, byte[] data){
        int lines = 0;
        while (cursor > 0){
            if(data[cursor] == '\n')++lines;
            --cursor;
        }
        return lines;
    }


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


    private static int countLinesIn(int cursor, char[] data){
        int lines = 0;
        while (cursor > 0){
            if(data[cursor] == '\n')++lines;
            --cursor;
        }
        return lines;
    }
}