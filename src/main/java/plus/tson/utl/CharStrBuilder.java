package plus.tson.utl;


public final class CharStrBuilder {
    private char[] bytes;
    private int length;

    public CharStrBuilder(int initLength) {
        this.bytes = new char[initLength];
    }

    public int length() {
        return length;
    }

    public void append(char b) {
        setLength(length);
        bytes[length] = b;
        ++length;
    }

    public void append(byte b){
        setLength(length);
        bytes[length] = (char) b;
        ++length;
    }


    public void clear(){
        length = 0;
    }


    public void setLength(int length) {
        if (length >= bytes.length) {
            char[] newByte = new char[length+16];
            System.arraycopy(bytes, 0, newByte, 0, bytes.length);
            bytes = newByte;
        }
        this.length = length;
    }

    @Override
    public String toString() {
        return new String(bytes, 0, length);
    }
}
