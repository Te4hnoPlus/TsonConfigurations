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


    /**
     * @return Current length
     */
    public final int getLength() {
        return length;
    }


    /**
     * Append char to the end
     */
    public final void append(char b) {
        setLength(length);
        chars[length++] = b;
    }


    /**
     * Append byte to the end
     */
    public final void append(byte b){
        setLength(length);
        chars[length++] = (char) b;
    }


    /**
     * Lazy clear the content
     */
    public final void clear(){
        length = 0;
    }


    /**
     * Set length and ensure capacity
     */
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


    public final String cString(){
        return new String(chars, 0, length);
    }
}
