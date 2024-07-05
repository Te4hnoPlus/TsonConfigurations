package plus.tson.utl;

import plus.tson.*;
import plus.tson.exception.TsonSyntaxException;


public class JScemParser {
    private final byte[] data;
    private final ByteStrBuilder b = new ByteStrBuilder(16);
    private int cursor = 0;

    public JScemParser(byte[] data) {
        this.data = data;
    }

    public <E> E result(JSceme<E> schem){
        E obj = schem.newObj();
        goTo('{');
        fillMap(schem, obj);
        return obj;
    }


    private void goTo(char chr){
        int cur;
        for(cur=cursor;cur<data.length;++cur){
            if(data[cur]==chr)break;
        }
        cursor = ++cur;
    }


    private void getItem(JSceme schem, Object obj, String key){
        switch (data[cursor++]){
            case '"':  getStr('"', schem, obj, key);break;
            case '\'': getStr('\'', schem, obj, key);break;
            case '{':  getMap0(schem, obj, key);break;
            case '[':  getList0(schem, obj, key);break;
            default:   getBasic(schem, obj, key);break;
        }
    }


    private String getErrorString(){
        int min = Math.max(0, cursor-50);
        int max = Math.min(cursor+50,data.length-1);
        byte[] bytes = new byte[max-min];
        System.arraycopy(data, min, bytes, 0, bytes.length);
        return new String(bytes);
    }


    private void getMap0(JSceme schem, Object obj, String key){
        JSceme sub = schem.sub(key);
        if(sub==null){
            schem.set(key, obj, getMap1());
        } else {
            Object subobj = sub.newObj();
            schem.set(key, obj, subobj);
            fillMap(sub, subobj);
        }
    }


    private void fillMap(JSceme schem, Object obj){
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
                key = getKey();
            } else {
                getItem(schem, obj, key);
                waitKey = true;
                waitSep = true;
            }
            cur = cursor;
        }
        cursor = cur;
    }


    private void getList0(JSceme schem, Object obj, String key){
        schem.set(key, obj, getList0());
    }


    private TsonList getList0(){
        TsonList list = new TsonList();

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
        return list;
    }


    public TsonObj getItem(){
        switch (data[cursor++]){
            case '"':  return new TsonStr(getStr0('"'));
            case '\'': return new TsonStr(getStr0('\''));
            case '{':  return getMap1();
            case '[':  return getList0();
            default:   return getBasic();
        }
    }


    private TsonMap getMap1(){
        TsonMap map = new TsonMap();
        fillMap1(map);
        return map;
    }


    private void fillMap1(TsonMap map){
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
                key = getKey();
            } else {
                map.fput(key, getItem());
                waitKey = true;
                waitSep = true;
            }
            cur = cursor;
        }
        cursor = cur;
    }


    private String getStr0(char end){
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
        return b.toString();
    }


    private void getStr(char end, JSceme schem, Object obj, String key){
        schem.set(key, obj, getStr0(end));
    }


    TsonPrimitive getBasic(){
        int cur;
        for(cur=cursor-1;cur<data.length;++cur){
            byte c = data[cur];
            if(c==' ' || c == '\n')continue;
            if(c > 47 && c < 58 || c == '-'){
                cursor = cur;
                return readNum1();
            }
            cursor = cur;
            return new TsonBool(readBool());
        }
        throw TsonSyntaxException.make(cur, data);
    }


    private TsonPrimitive readNum1(){
        int cur, num, size = 0;
        boolean invert;
        if(invert = (data[cursor] == '-')) ++cursor;
        num = data[cursor]-48;
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
                    return new TsonDouble(readLongNum());
            }
            cursor = cur;
            if(size>6){
                return new TsonDouble(num2/dec1);
            } else {
                return new TsonFloat((float) (num2/dec1));
            }
        } else {
            cursor = cur-1;
            return invert?new TsonInt(-num):new TsonInt(num);
        }
    }


    void getBasic(JSceme schem, Object obj, String key){
        int cur;
        for(cur=cursor-1;cur<data.length;++cur){
            byte c = data[cur];
            if(c==' ' || c == '\n')continue;
            if(c > 47 && c < 58 || c == '-'){
                cursor = cur;
                readNum(schem, obj, key);return;
            }
            cursor = cur;
            schem.set(key, obj, readBool());
            return;
        }
        throw TsonSyntaxException.make(cur, data);
    }


    private boolean readBool(){
        if(isTrue()){
            cursor += 3;
            return true;
        }
        if(isFalse()){
            cursor += 4;
            return false;
        }
        throw TsonSyntaxException.make(cursor, data, "Wrong value");
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


    private double readLongNum(){
        int cur;
        b.setLength(0);
        for(cur=cursor;cur<data.length;++cur){
            byte c = data[cur];
            if(c == ' '){
                do {
                    ++cur;
                    c = data[cur];
                } while (c == ' ');
                break;
            } else {
                b.append(c);
            }
        }
        cursor = cur;
        return Double.parseDouble(b.toString());
    }


    private void readLongNum(JSceme schem, Object obj, String key){
        schem.set(key, obj, readLongNum());
    }


    private void readNum(JSceme schem, Object obj, String key){
        int cur, num, size = 0;
        boolean invert;
        if(invert = (data[cursor] == '-')) ++cursor;
        num = data[cursor]-48;
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
                } else if(c!='_'){
                    readLongNum(schem, obj, key);
                    return;
                }
            }
            cursor = cur;
            if(size>6){
                schem.set(key, obj, num2/dec1);
            } else {
                schem.set(key, obj, (float) (num2/dec1));
            }
        } else {
            cursor = cur-1;
            schem.set(key, obj, invert?-num:num);
        }
    }


    private String getKey(){
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