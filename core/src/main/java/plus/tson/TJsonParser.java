package plus.tson;

import plus.tson.exception.TsonSyntaxException;
import plus.tson.utl.ByteStrBuilder;


/**
 * Alternative parser for creating a hierarchy of Tson objects from a Json string
 * <br><br>
 * Usage example:
 * <pre>
 * {@code new TJsonParser("{key: 10}").getMap()}
 * </pre>
 */
public final class TJsonParser {
    private final boolean objMode;
    private final char[] data;
    private final ByteStrBuilder b = new ByteStrBuilder(16);
    int cursor = 0;

    public TJsonParser(byte[] data, boolean objMode) {
        this.data = new String(data).toCharArray();
        this.objMode = objMode;
    }


    public TJsonParser(String s){
        this.data = s.toCharArray();
        objMode = false;
    }


    public TJsonParser(String s, boolean objMode){
        this.data = s.toCharArray();
        this.objMode = objMode;
    }


    public TJsonParser(byte[] data){
        this.data = new String(data).toCharArray();
        objMode = false;
    }


    public void goTo(char chr){
        int cur;
        for(cur=cursor;cur<data.length;++cur)
            if(data[cur] == chr)break;
        cursor = ++cur;
    }


    public TsonObj getItem(){
        switch (data[cursor++]){
            case '"':  return getStr('"');
            case '\'': return getStr('\'');
            case '{':  return getMap0();
            case '[':  return getList0();
            default:   return getBasic();
        }
    }


    private TsonMap getMap0(){
        TsonMap map = new TsonMap();
        fillMap(map);
        return map;
    }


    public TsonMap getMap(){
        TsonMap map = new TsonMap();
        goTo('{');
        fillMap(map);
        return map;
    }


    public void fillMap(TsonMap map){
        final char[] data = this.data;
        final int length = data.length;
        int cur = cursor;
        boolean waitSep = false, waitKey = true;
        String key = null;

        for(char c; cur < length; ++cur){
            if((c = data[cur]) == '}'){
                cursor = cur + 1;
                return;
            }
            if(c == ' ' || c == '\n') continue;
            if(waitSep && c == ','){
                waitSep = false;
                continue;
            }
            cursor = cur;
            if(waitKey) {
                waitKey = false;
                key = objMode?getObjKey(data):getKey(data);
            } else {
                map.fput(key, getItem());
                waitKey = true;
                waitSep = true;
            }
            cur = cursor;
        }
        cursor = cur;
    }


    private TsonList getList0(){
        TsonList list = new TsonList();
        fillList(list);
        return list;
    }


    public TsonList getList(){
        TsonList list = new TsonList();
        goTo('[');
        fillList(list);
        return list;
    }


    private void fillList(TsonList list){
        final char[] data = this.data;
        final int length = data.length;
        int cur = cursor;
        char c;
        for(;cur < length; ++cur){
            if((c = data[cur]) == ']') {
                cursor = cur;
                return;
            }
            if(c == ' ' || c == '\n')continue;
            cursor = cur;
            list.add(getItem());
            cur = cursor;
            break;
        }
        for(boolean waitSep = true; cur < length; ++cur){
            if((c = data[cur]) == ']') {
                cursor = cur;
                return;
            }
            if(c == ' ' || c == '\n') continue;
            if(waitSep && c == ','){
                waitSep = false;
                continue;
            }
            cursor = cur;
            list.add(getItem());
            cur = cursor;
            waitSep = true;
        }
    }


    private TsonStr getStr(char end){
        int cur = cursor;
        b.clear();
        for(char c; cur < data.length; ++cur){
            if((c = data[cur]) == end)break;
            if(c == '\\'){
                ++cur;
                continue;
            }
            b.append(c);
        }
        cursor = cur;
        return new TsonStr(b.toString());
    }


    TsonPrimitive getBasic(){
        final char[] data = this.data;
        int cur = cursor-1;
        for(char c; cur < data.length; ++cur){
            if((c = data[cur]) == ' ' || c == '\n')continue;
            if(c > 47 && c < 58 || c == '-'){
                cursor = cur;
                return readNum(data);
            }
            cursor = cur;
            return readBool(data);
        }
        throw TsonSyntaxException.make(cur, data);
    }


    private TsonPrimitive readBool(final char[] data){
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


    private boolean isFalse(final char[] data, final int cursor){
        char cur = data[cursor];
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


    private boolean isTrue(final char[] data, final int cursor){
        char cur = data[cursor];
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


    private TsonPrimitive readNum(final char[] data){
        boolean invert, dec = false;
        if(invert = (data[cursor] == '-')) ++cursor;
        long num = data[cursor] - 48;
        int cur = cursor + 1, size = 0;

        for(char c; cur < data.length; ++cur){
            if((c = data[cur]) > 47 && c < 58) {
                num = num * 10 + (c-48);
                ++size;
            } else {
                if (c == '.') {
                    dec = true;
                    ++cur;
                    break;
                }
                if (c == ' ') {
                    do {
                        ++cur;
                        c = data[cur];
                    } while (c == ' ');
                    break;
                }
                if (c == ',' || c == '}' || c == ']') {
                    //++cur;
                    break;
                }
            }
        }
        if(dec){
            double num2 = num;
            int dec1 = invert?-1:1;
            for(char c;cur<data.length;++cur){
                c = data[cur];
                if(c > 47 && c < 58) {
                    num2 = num2 * 10 + (c - 48);
                    dec1 *= 10;
                    ++size;
                }
                else if(c == ' '){
                    do {
                        ++cur;
                        c = data[cur];
                    } while (c == ' ');
                    break;
                } else if(c!='_')
                    throw TsonSyntaxException.make(cur, data,"Number format error");
            }
            cursor = cur;
            if(size > 6) return new TsonDouble(num2 / dec1);
            else return new TsonFloat((float) (num2 / dec1));
        } else {
            cursor = cur - 1;
            if(size > 10)
                return invert?new TsonLong(-num):new TsonLong(num);
            else
                return invert?new TsonInt((int) -num):new TsonInt((int) num);
        }
    }


    private String getKey(final char[] data){
        int cur = cursor;
        if(data[cur] != '"') throw TsonSyntaxException.make(cursor, data, "Wait '\"', but '" + ((char)data[cur]) + "'");
        int start = ++cur;
        l1: for(final int length = data.length; cur < length; ++cur){
            if(data[cur] == '"'){
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
        cursor = cur;
        return new String(data, start, cur - start - 1);
    }


    private String getObjKey(final char[] data){
        //final byte[] data = this.data;
        final int length = data.length;
        ByteStrBuilder b = this.b;
        int cur = cursor;
        b.clear();
        for(char c; cur < length; ++cur){
            if((c = data[cur]) == ':') break;
            if(c == ' ' || c == '\n')continue;
            b.append(c);
        }
        cursor = cur;
        return b.toString();
    }
}