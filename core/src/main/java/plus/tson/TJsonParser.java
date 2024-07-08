package plus.tson;

import plus.tson.exception.TsonSyntaxException;
import plus.tson.utl.ByteStrBuilder;
import java.nio.charset.StandardCharsets;


/**
 * Alternative parser for creating a hierarchy of Tson objects from a Json string
 * <br><br>
 * Usage example:
 * <pre>
 * {@code new TJsonParser("{\"key\": 10}").getMap()}
 * </pre>
 */
public final class TJsonParser extends ByteStrBuilder{
    private final boolean objMode;
    private final byte[] data;
    private int cursor = 0;

    public TJsonParser(final String str) {
        this(str.getBytes(StandardCharsets.UTF_8), false);
    }


    public TJsonParser(final String str, final boolean objMode) {
        this(str.getBytes(StandardCharsets.UTF_8), objMode);
    }


    public TJsonParser(final byte[] data, final boolean objMode) {
        super(16);
        this.objMode = objMode;
        this.data = data;
    }


    public TsonMap getMap(){
        final byte[] data = this.data;
        for (int i = 0, s = data.length; i < s;i++){
            if(data[i] == '{') return getMap(data, this.cursor = i+1);
        }
        return null;
    }


    public void fillMap(final TsonMap map){
        final byte[] data = this.data;
        for (int i = 0, s = data.length; i < s; i++){
            if(data[i] == '{'){
                fillMap(map, data, this.cursor = i+1, this.objMode);
                return;
            }
        }
    }


    public void fillList(final TsonList list){
        final byte[] data = this.data;
        for (int i = 0, s = data.length; i < s; i++){
            if(data[i] == '['){
                fillList(list, data, this.cursor = i+1);
                return;
            }
        }
    }


    private TsonMap getMap(final byte[] data, final int cursor){
        final TsonMap map;
        fillMap(map = new TsonMap(), data, cursor, this.objMode);
        return map;
    }


    private void fillMap(final TsonMap map, final byte[] data, int cursor, final boolean objMode){
        final int length = data.length;
        boolean waitSep = false, waitKey = true;
        String key = null;
        for (byte chr; cursor < length; cursor++){
            if((chr = data[cursor]) == '}'){
                this.cursor = cursor+1;
                return;
            }
            if(chr == ' ' || chr == '\n')continue;
            if(waitSep) {
                if(chr == ',') waitSep = false;
            }else if(waitKey){
                if(objMode) key = getObjKey(data, cursor);
                else        key = getKey(data, cursor);
                cursor = this.cursor;
                waitKey = false;
            } else {
                this.cursor = cursor;
                map.fput(key, getItem(data, chr));
                cursor = this.cursor-1;
                waitSep = true;
                waitKey = true;
            }
        }
    }


    private TsonList getList(final byte[] data, int cursor){
        final TsonList list;
        fillList(list = new TsonList(), data, cursor);
        return list;
    }


    private void fillList(final TsonList list, final byte[] data, int cursor){
        boolean waitSep = false;
        byte chr;
        for (final int length = data.length; cursor < length; cursor++){
            if((chr = data[cursor]) == ']'){
                this.cursor = cursor+1;
                return;
            }
            if(chr == ' ' || chr == '\n')continue;
            if(waitSep) {
                if(chr == ',') waitSep = false;
                continue;
            }
            this.cursor = cursor;
            list.add(getItem(data, chr));
            cursor = this.cursor-1;
            waitSep = true;
        }
    }


    public TsonObj getItem(final byte[] data, final byte chr){
        switch (chr){
            case '"' : return getStr(data,'"');
            case '\'': return getStr(data,'\'');
            case '{' : return getMap(data, ++cursor);
            case '[' : return getList(data, ++cursor);
            case '-' : return getNum(data, ++cursor, true);
            default: {
                if(chr >= '0' && chr <= '9')return getNum(data, cursor, false);
                return readBool(data);
            }
        }
    }


    private TsonObj getNum(final byte[] data, int cursor, final boolean invert){
        boolean sep = false;
        final int length = data.length;
        int num = 0;
        byte size = 0;

        byte c;
        for (;cursor < length && size < 9; cursor++){
            if((c = data[cursor]) > 47 && c < 58) {
                num = (num * 10) + (c-48);
                ++size;
                continue;
            }
            if(c == '_')continue;
            if(c == '.'){
                ++cursor;
                sep = true;
            }
            break;
        }
        if(size == 9){
            long lNum = num;
            for (;cursor < length; cursor++, size++){
                if(size > 18)throw TsonSyntaxException.make(cursor, data, "Number '"+lNum+((char)data[cursor])+"..' is too big");
                if((c = data[cursor]) > 47 && c < 58) {
                    num = num * 10 + (c-48);
                    ++size;
                }
                if(c == '_')continue;
                if(c == '.'){
                    ++cursor;
                    sep = true;
                    break;
                }
            }
            if(sep){
                return readDel(data, cursor, num, size, invert);
            } else {
                this.cursor = cursor;
                if(invert)return new TsonLong(-lNum);
                return new TsonLong(lNum);
            }
        }

        if(sep){
            return readDel(data, cursor, num, size, invert);
        } else {
            this.cursor = cursor;
            if(invert)return new TsonInt(-num);
            return new TsonInt(num);
        }
    }


    private TsonObj readDel(final byte[] data, int cursor, long lNum, byte size, final boolean invert){
        final int length = data.length;
        int pSize = 0, nSize = 1;

        for (byte c ;cursor < length; cursor++){
            if((c = data[cursor]) > 47 && c < 58) {
                if(++size > 18){
                    throw TsonSyntaxException.make(cursor, data, "Number '"+(((double)lNum)/pSize)+((char)data[cursor])+"..' is too big");
                }
                lNum = lNum * 10 + (c-48);
                pSize = nSize;
                nSize *= 10;
                continue;
            }
            if(c == '_')continue;
            break;
        }
        this.cursor = cursor;
        if(size > 8){
            if(invert)return new TsonDouble(-(((double)lNum)/pSize));
            return new TsonDouble(((double)lNum)/pSize);
        } else {
            if(invert)return new TsonFloat(-((float)(((double)lNum)/pSize)));
            return new TsonFloat((float)(((double)lNum)/pSize));
        }
    }


    private TsonPrimitive readBool(final byte[] data){
        if(isTrue(data, cursor)){
            cursor += 3;
            return TsonBool.TRUE;
        }
        if(isFalse(data, cursor)){
            cursor += 4;
            return TsonBool.FALSE;
        }
        throw TsonSyntaxException.make(cursor, data,"Wait 'true' or 'false', but '"+((char)data[cursor])+"'");
    }


    private static boolean isFalse(final byte[] data, final int cursor){
        final byte cur = data[cursor];
        if(cur == 'f')
            return data[cursor + 1] == 'a' && data[cursor + 2] == 'l' && data[cursor + 3] == 's' && data[cursor + 4] == 'e';
        if(cur == 'F'){
            if(data[cursor + 1] == 'a')
                return data[cursor + 2] == 'l' && data[cursor + 3] == 's' && data[cursor + 4] == 'e';
            else if(data[cursor + 1] == 'A')
                return data[cursor + 2] == 'L' && data[cursor + 3] == 'S' && data[cursor + 4] == 'E';
        }
        return false;
    }


    private static boolean isTrue(final byte[] data, final int cursor){
        final byte cur = data[cursor];
        if(cur == 't')
            return data[cursor + 1] == 'r' && data[cursor + 2] == 'u' && data[cursor + 3] == 'e';
        if(cur == 'T'){
            if(data[cursor+1] == 'r')
                return data[cursor + 2] == 'u' && data[cursor + 3] == 'e';
            else if(data[cursor+1] == 'R')
                return data[cursor + 2] == 'U' && data[cursor + 3] == 'E';
        }
        return false;
    }


    private TsonStr getStr(final byte[] data, final char end){
        final int length = data.length;
        clear();
        int cur = cursor+1;
        for(byte c; cur < length; ++cur){
            if((c = data[cur]) == end)break;
            if(c == '\\'){
                ++cur;
                continue;
            }
            append(c);
        }
        this.cursor = cur;
        return new TsonStr(cString());
    }


    private String getKey(final byte[] data, int cur){
        if(data[cur] != '"') throw TsonSyntaxException.make(cur, data, "Wait '\"', but '" + ((char)data[cur]) + "'");
        final int start = ++cur;
        int end = start;
        l1: for(final int length = data.length; cur < length; ++cur){
            if(data[cur] == '"'){
                end = cur;
                ++cur;
                while (cur < length){
                    if(data[cur] == ':'){
                        break l1;
                    }
                    ++cur;
                }
                break;
            }
        }
        this.cursor = cur;
        return new String(data, start, end - start, StandardCharsets.UTF_8);
    }


    private String getObjKey(final byte[] data, int cursor){
        clear();
        byte c;
        for(final int length = data.length; cursor < length; ++cursor){
            if((c = data[cursor]) == ':') break;
            if(c == ' ' || c == '\n')continue;
            append(c);
        }
        this.cursor = cursor;
        return cString();
    }
}