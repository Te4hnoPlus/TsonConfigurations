package plus.tson.utl;

import plus.tson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;


/**
 * Write/read Tson-Obects to/from binary stream
 */
public class TsonBinCoder {
    public static void writeAll(OutputStream out, TsonObj obj) throws IOException {
        DataOutputStream out1 = new DataOutputStream(out);
        write(out1, obj);
        out1.flush();
        out1.close();
    }


    public static TsonObj readAll(InputStream in) throws IOException {
        DataInputStream in1 = new DataInputStream(in);
        TsonObj obj = read(in1);
        in1.close();
        return obj;
    }


    private static void write(DataOutputStream out, TsonObj obj) throws IOException {
        if(obj.isCustom())throw new RuntimeException("Custom type is not supported");
        wryteType(out, obj.type().ordinal());
        write0(out, obj);
    }


    private static void write0(DataOutputStream out, TsonObj obj) throws IOException {
        switch (obj.type()){
            case INT   : out.writeInt(obj.getInt())      ;break;
            case LONG  : out.writeLong(obj.getLong())    ;break;
            case FLOAT : out.writeFloat(obj.getFloat())  ;break;
            case DOUBLE: out.writeDouble(obj.getDouble());break;
            case STRING: writeStr(out, obj.getStr())     ;break;
            case BOOL  : out.writeBoolean(obj.getBool()) ;break;
            case LIST  : writeList(out, obj.getList())   ;break;
            case MAP   : writeMap(out, obj.getMap())     ;break;

            default: throw new RuntimeException("Illegal type: " + obj.type());
        }
    }


    private static TsonObj read(DataInputStream in) throws IOException {
        TsonObj.Type type = TsonObj.Type.valueOfC(readType(in));
        switch (type){
            case INT:    return new TsonInt(in.readInt());
            case LONG:   return new TsonLong(in.readLong());
            case FLOAT:  return new TsonFloat(in.readFloat());
            case DOUBLE: return new TsonDouble(in.readDouble());
            case STRING: return new TsonStr(readStr(in));
            case BOOL:   return new TsonBool(in.readBoolean());
            case LIST:   return readList(in);
            case MAP:    return readMap(in);
        }
        return null;
    }


    private static void writeMap(DataOutputStream out,  TsonMap map) throws IOException {
        out.writeInt(map.size());
        map.forEach((key, value) -> {
            try {
                writeStr(out, key);
                write(out, value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    private static TsonMap readMap(DataInputStream in) throws IOException {
        int size = in.readInt();
        TsonMap map = new TsonMap();
        for (int i = 0; i < size; i++) {
            map.fput(readStr(in), read(in));
        }
        return map;
    }


    private static void writeList(DataOutputStream out,  TsonList list) throws IOException {
        out.writeInt(list.size());
        list.forEach(obj -> {
            try {
                write(out, obj);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    private static TsonList readList(DataInputStream in) throws IOException {
        int size = in.readInt();
        TsonList list = new TsonList();
        for (int i = 0; i < size; i++) {
            list.add(read(in));
        }
        return list;
    }


    private static String readStr(DataInputStream in) {
        try {
            int size = in.readUnsignedShort();
            byte[] bytes = new byte[size];
            in.readFully(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void writeStr(DataOutputStream out, String str) {
        try {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            out.writeShort(bytes.length);
            out.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void wryteType(DataOutputStream out, int type){
        try {
            out.writeByte(type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static int readType(DataInputStream in) {
        try {
            return in.readUnsignedByte();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
