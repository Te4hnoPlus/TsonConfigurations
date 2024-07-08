package plus.tson.utl;


/**
 * The faster equivalent of StringBuilder
 */
public class CharStrBuilder {
    private char[] chars;
    private int length;

    public CharStrBuilder(int initLength) {
        this.chars = new char[initLength];
    }


    public final int length() {
        return length;
    }


    public final void append(char b) {
        setLength(length);
        chars[length++] = b;
    }


    public final void append(byte b){
        setLength(length);
        chars[length++] = (char) b;
    }


    public final void clear(){
        length = 0;
    }


    public final void setLength(int length) {
        if (length >= chars.length) {
            char[] newByte = new char[length+16];
            System.arraycopy(chars, 0, newByte, 0, chars.length);
            chars = newByte;
        }
        this.length = length;
    }


    @Override
    public String toString() {
        return cString();
    }


    protected final String cString(){
        return new String(chars, 0, length);
    }
}
