package plus.tson.utl;

import java.nio.charset.StandardCharsets;


public final class ByteStrBuilder {
    private byte[] bytes;
    private int length;

    public ByteStrBuilder(int initLength) {
        this.bytes = new byte[initLength];
    }

    public int getLength() {
        return length;
    }

    public void append(byte b){
        setLength(length);
        bytes[length] = b;
        ++length;
    }

    public void append(char b) {
        setLength(length);
        bytes[length] = (byte) b;
        ++length;
    }

    public void setLength(int length) {
        if(length >= bytes.length){
            byte[] newByte = new byte[length+16];
            System.arraycopy(bytes, 0, newByte, 0, bytes.length);
            bytes = newByte;
        }
        this.length = length;
    }

    @Override
    public String toString() {
        return new String(bytes, 0, length, StandardCharsets.UTF_8);
    }
}
