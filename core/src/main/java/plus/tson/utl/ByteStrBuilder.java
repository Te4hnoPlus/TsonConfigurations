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


    public final int getLength() {
        return length;
    }


    public final void append(byte b){
        setLength(length);
        bytes[length++] = b;
    }


    public final void append(char b) {
        setLength(length);
        bytes[length++] = (byte) b;
    }


    public final void clear(){
        length = 0;
    }


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


    protected final String cString(){
        return new String(bytes, 0, length, StandardCharsets.UTF_8);
    }
}
