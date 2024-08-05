package plus.tson.utl;

import java.nio.charset.StandardCharsets;


/**
 * The faster equivalent of StringBuilder
 */
public class ByteStrBuilder {
    private byte[] bytes;
    private int length;

    public ByteStrBuilder(int initLength) {
        this.bytes = new byte[initLength];
    }


    /**
     * @return Current length
     */
    public final int getLength() {
        return length;
    }


    /**
     * Append byte to the end
     */
    public final void append(byte b){
        setLength(length);
        bytes[length++] = b;
    }


    /**
     * Append char to the end
     */
    public final void append(char b) {
        setLength(length);
        bytes[length++] = (byte) b;
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
        if(length >= bytes.length){
            byte[] newByte = new byte[length+16];
            System.arraycopy(bytes, 0, newByte, 0, bytes.length);
            bytes = newByte;
        }
        this.length = length;
    }


    @Override
    public String toString() {
        return cString();
    }


    public final String cString(){
        return new String(bytes, 0, length, StandardCharsets.UTF_8);
    }
}
