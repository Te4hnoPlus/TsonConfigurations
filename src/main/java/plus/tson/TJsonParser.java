package plus.tson;

import plus.tson.exception.TsonSyntaxException;
import plus.tson.utl.ByteStrBuilder;


public final class TJsonParser {
    private final boolean objMode;
    private final byte[] data;
    private final ByteStrBuilder b = new ByteStrBuilder(16);
    private int cursor = 0;

    public TJsonParser(byte[] data, boolean objMode) {
        this.data = data;
        this.objMode = objMode;
    }


    public TJsonParser(String s){
        this(s.getBytes());
    }


    public TJsonParser(String s, boolean objMode){
        this(s.getBytes(), objMode);
    }


    public TJsonParser(byte[] data){
        this(data, false);
    }


    private void goTo(char chr){
        int cur;
        for(cur=cursor;cur<data.length;++cur){
            if(data[cur]==chr)break;
        }
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


    private String getErrorString(){
        int min = Math.max(0, cursor-50);
        int max = Math.min(cursor+50,data.length-1);
        byte[] bytes = new byte[max-min];
        System.arraycopy(data, min, bytes, 0, bytes.length);
        return new String(bytes);
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


    private void fillMap(TsonMap map){
        int cur;
        boolean waitSep = false;
        boolean waitKey = true;
        String key = null;
        for(cur = cursor;cur<data.length;++cur){
            byte c = data[cur];
            if(c=='}')break;
            if(waitSep || c==' ' || c == '\n'){
                if(c==',')waitSep = false;
                continue;
            }
            cursor = cur;
            if(waitKey) {
                waitKey = false;
                key = objMode?getObjKey():getKey();
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
        boolean first = true;
        boolean waitSep = true;
        for(int cur=cursor;cur<data.length;++cur){
            byte c = data[cur];
            if(c == ']') {
                cursor = cur;
                break;
            }
            if(first){
                if(c==' ' || c == '\n')continue;
                cursor = cur;
                list.add(getItem());
                cur = cursor;
                first = false;
            } else {
                if(waitSep || c==' ' || c == '\n'){
                    if(c==',')waitSep = false;
                    continue;
                }
                cursor = cur;
                list.add(getItem());
                cur = cursor;
                waitSep = true;
            }
        }
    }


    private TsonStr getStr(char end){
        int cur;
        b.setLength(0);
        for(cur=cursor;cur<data.length;++cur){
            byte c = data[cur];
            if(c==end)break;
            if(c=='\\'){
                ++cur;
                continue;
            }
            b.append(c);
        }
        cursor = cur;
        return new TsonStr(b.toString());
    }


    TsonPrimitive getBasic(){
        int cur;
        for(cur=cursor-1;cur<data.length;++cur){
            byte c = data[cur];
            if(c==' ' || c == '\n')continue;
            if(c > 47 && c < 58 || c == '-'){
                cursor = cur;
                return readNum();
            }
            cursor = cur;
            return readBool();
        }
        throw new TsonSyntaxException(getErrorString(), cur, data[cursor]);
    }


    private TsonPrimitive readBool(){
        if(isTrue()){
            cursor += 3;
            return TsonBool.TRUE;
        }
        if(isFalse()){
            cursor += 4;
            return TsonBool.FALSE;
        }
        throw new TsonSyntaxException(getErrorString(), cursor, "Wrong value");
    }


    private boolean isFalse(){
        byte cur = data[cursor];
        if(cur=='f'){
            return data[cursor+1]=='a' && data[cursor+2]=='l' && data[cursor+3]=='s' && data[cursor+4]=='e';
        }
        if(cur=='F'){
            if(data[cursor+1]=='a'){
                return data[cursor+2]=='l' && data[cursor+3]=='s' && data[cursor+4]=='e';
            } else if(data[cursor+1]=='A'){
                return data[cursor+2]=='L' && data[cursor+3]=='S' && data[cursor+4]=='E';
            }
        }
        return false;
    }


    private boolean isTrue(){
        byte cur = data[cursor];
        if(cur=='t'){
            return data[cursor+1]=='r' && data[cursor+2]=='u' && data[cursor+3]=='e';
        }
        if(cur=='T'){
            if(data[cursor+1]=='r'){
                return data[cursor+2]=='u' && data[cursor+3]=='e';
            } else if(data[cursor+1]=='R'){
                return data[cursor+2]=='U' && data[cursor+3]=='E';
            }
        }
        return false;
    }


    private TsonPrimitive readNum(){
        int cur, size = 0;
        boolean invert;
        if(invert = (data[cursor] == '-')) ++cursor;
        long num = data[cursor]-48;
        boolean dec = false;

        for(cur=cursor+1;cur<data.length;++cur){
            byte c = data[cur];
            if(c>47 && c < 58) {
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
                if (c == ',' || c == '}' || c == ']') break;
            }
        }
        if(dec){
            double num2 = num;
            int dec1 = invert?-1:1;
            for(;cur<data.length;++cur){
                byte c = data[cur];
                if(c>47 && c < 58) {
                    num2 = num2 * 10 + (c-48);
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
                    throw new TsonSyntaxException(getErrorString(), cur, "Number format error");
            }
            cursor = cur;
            if(size>6){
                return new TsonDouble(num2/dec1);
            } else {
                return new TsonFloat((float) (num2/dec1));
            }
        } else {
            cursor = cur-1;
            if(size>10)
                return invert?new TsonLong(-num):new TsonLong(num);
            else
                return invert?new TsonInt((int) -num):new TsonInt((int) num);
        }
    }


    private String getKey(){
        int cur = cursor;
        b.setLength(0);
        if(data[cur] != '"') throw new TsonSyntaxException(getErrorString(), cursor, "Expected [ \" ]");
        ++cur;
        for(;cur<data.length;++cur){
            byte c = data[cur];
            if(c=='"'){
                ++cur;
                break;
            }
            b.append(c);
        }
        cursor = cur;
        return b.toString();
    }


    private String getObjKey(){
        int cur = cursor;
        b.setLength(0);
        for(;cur<data.length;++cur){
            byte c = data[cur];
            if(c==':')break;
            if(c==' ' || c == '\n')continue;
            b.append(c);
        }
        cursor = cur;
        return b.toString();
    }
}