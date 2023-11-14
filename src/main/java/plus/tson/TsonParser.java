package plus.tson;

import plus.tson.exception.NoSearchException;
import plus.tson.exception.TsonSyntaxException;
import plus.tson.security.ClassManager;
import plus.tson.utl.CharStrBuilder;
import java.util.ArrayList;


public final class TsonParser {
    private final ClassManager manager;
    private final char[] data;
    private final CharStrBuilder b = new CharStrBuilder(16);
    private int cursor = 0;

    public TsonParser(String data) {
        this.manager = new ClassManager.Def();
        this.data = data.toCharArray();
    }


    public TsonParser(ClassManager manager, String data) {
        this.manager = manager;
        this.data = data.toCharArray();
    }


    TsonParser goToFirst(){
        int cur = cursor;
        for(char c;cur<data.length;++cur){
            if(!((c = data[cur]) == ' ' || c == '\n'))break;
        }
        cursor = cur;
        return this;
    }


    TsonParser goTo(char chr){
        int cur;
        for(cur=cursor;cur<data.length;++cur){
            if(data[cur]==chr)break;
        }
        cursor = ++cur;
        return this;
    }


    private TsonObj getItem(){
        switch (data[cursor++]){
            case '"':  return getStr('"');
            case '\'': return getStr('\'');
            case '(':  return getBasic();
            case '{':  return getMap();
            case '[':  return getList();
            case '<':  return getField();
        }
        --cursor;
        if(isTrue()){
            cursor+=3;
            return TsonBool.TRUE;
        }
        if(isFalse()){
            cursor+=4;
            return TsonBool.FALSE;
        }
        throw new TsonSyntaxException(getErrorString(), cursor, data[cursor]);
    }


    private String getErrorString(){
        int min = Math.max(0, cursor-50);
        int max = Math.min(cursor+50,data.length-1);
        char[] chars = new char[max-min];
        System.arraycopy(data, min, chars, 0, chars.length);
        return new String(chars);
    }


    private TsonMap getMap(){
        TsonMap map = new TsonMap();
        fillMap(map);
        return map;
    }


    void fillMap(TsonMap map){
        int cur = cursor;
        boolean waitSep = false, waitKey = true;
        String key = null;
        for(char c;cur<data.length;++cur){
            if((c = data[cur]) == '}')break;
            if(waitSep || c == ' ' || c == '\n'){
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


    TsonList getList(){
        TsonList list = new TsonList();
        fillList(list);
        return list;
    }


    void fillList(TsonList list){
        int cur=cursor;
        char c;
        for(;cur<data.length;++cur){
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
        for(boolean waitSep = true;cur<data.length;++cur){
            if((c = data[cur]) == ']') {
                cursor = cur;
                return;
            }
            if(waitSep || c == ' ' || c == '\n'){
                if(c == ',')waitSep = false;
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
        boolean prevEcran = false;
        b.setLength(0);
        for(char c;cur<data.length;++cur){
            if((c = data[cur]) == end && !prevEcran)break;
            if(c == '\\'){
                prevEcran = true;
                continue;
            }
            b.append(c);
            prevEcran = false;
        }
        cursor = cur;
        return new TsonStr(b.toString());
    }


    TsonPrimitive getBasic(){
        int cur = cursor;
        for(char c;cur<data.length;++cur){
            if((c = data[cur]) == ' ' || c == '\n')continue;
            if(c > 47 && c < 58 || c == '-'){
                cursor = cur;
                return readNum();
            }
            if(c == ')')break;
            else {
                cursor = cur;
                return readClassOrBool();
            }
        }
        throw new TsonSyntaxException(getErrorString(), cur, data[cur]);
    }


    private boolean isFalse(){
        char cur = data[cursor];
        if(cur == 'f')
            return data[cursor+1]=='a' && data[cursor+2]=='l' && data[cursor+3]=='s' && data[cursor+4]=='e';
        if(cur == 'F'){
            if(data[cursor+1] == 'a')
                return data[cursor+2]=='l' && data[cursor+3]=='s' && data[cursor+4]=='e';
            else if(data[cursor+1] == 'A')
                return data[cursor+2]=='L' && data[cursor+3]=='S' && data[cursor+4]=='E';
        }
        return false;
    }


    private boolean isTrue(){
        char cur = data[cursor];
        if(cur == 't')
            return data[cursor+1]=='r' && data[cursor+2]=='u' && data[cursor+3]=='e';
        if(cur == 'T'){
            if(data[cursor+1] == 'r'){
                return data[cursor+2]=='u' && data[cursor+3]=='e';
            } else if(data[cursor+1]=='R'){
                return data[cursor+2]=='U' && data[cursor+3]=='E';
            }
        }
        return false;
    }


    private void skipAb(){
        while (cursor<=data.length) {
            if(data[cursor] == ')')break;
            ++cursor;
        }
    }


    private TsonPrimitive readClassOrBool(){
        if(isTrue()){
            cursor+=3;
            skipAb();
            return TsonBool.TRUE;
        }
        if(isFalse()){
            cursor += 4;
            skipAb();
            return TsonBool.FALSE;
        }
        int cur = cursor;
        b.setLength(0);
        for(char c;cur<=data.length;++cur){
            if((c = data[cur]) == ')')break;
            b.append(c);
        }
        cursor = cur;
        try {
            return new TsonClass(manager, b.toString());
        } catch (NoSearchException e){
            throw new TsonSyntaxException(getErrorString(), cur, e.getMessage());
        }
    }


    private TsonPrimitive readLongNum(){
        int cur = cursor;
        b.setLength(0);
        for(char c;cur<=data.length;++cur){
            if((c = data[cur]) == ')') break;
            else if(c == ' '){
                do {
                    ++cur;
                    c = data[cur];
                } while (c == ' ');
                if(c==')') break;
                throw new TsonSyntaxException(getErrorString(), cur, c);
            } else
                b.append(c);
        }
        cursor = cur;
        return new TsonDouble(Double.parseDouble(b.toString()));
    }


    private TsonPrimitive readNum(){
        boolean invert, dec = false;
        if(invert = (data[cursor] == '-')) ++cursor;
        int cur = cursor+1, num = data[cursor]-48, size = 0;

        for(char c;cur<=data.length;++cur){
            if((c = data[cur]) > 47 && c < 58) {
                num = num * 10 + (c-48);
                ++size;
            } else if(c == '.'){
                dec = true;
                ++cur;
                break;
            }
            else if(c == ')') break;
            else if(c == ' '){
                do {
                    ++cur;
                    c = data[cur];
                } while (c == ' ');
                if(c==')') break;
                throw new TsonSyntaxException(getErrorString(), cur, c);
            } else if(c!='_')return readLongNum();
        }
        if(dec){
            double num2 = num;
            int dec1 = invert?-1:1;
            for(;cur<=data.length;++cur){
                char c = data[cur];
                if(c>47 && c < 58) {
                    num2 = num2 * 10 + (c-48);
                    dec1 *= 10;
                    ++size;
                } else {
                    if(c == ')') break;
                    if(c == ' ') {
                        do {
                            ++cur;
                            c = data[cur];
                        } while (c == ' ');
                        if (c == ')') break;
                        throw new TsonSyntaxException(getErrorString(), cur, c);
                    }
                    if(c!='_')return readLongNum();
                }
            }
            cursor = cur;
            if(size>6)
                return new TsonDouble(num2/dec1);
            else
                return new TsonFloat((float) (num2/dec1));
        } else {
            cursor = cur;
            return invert?new TsonInt(-num):new TsonInt(num);
        }
    }


    Object getFieldObj(){
        TsonClass tsonClass = getTsonClass();
        boolean isList, waitSep = true;
        ArrayList list;
        if(isList = tsonClass.getField() == TsonObj.class)
            list = new TsonList();
        else
            list = new ArrayList<>();
        for(int cur = cursor;cur<=data.length;++cur){
            char c = data[cur];
            if(c == '>') break;
            if(waitSep){
                if(c == ',')waitSep = false;
                continue;
            }
            if(c == ' ' || c == '\n')continue;
            cursor = cur;
            list.add(isList?getItem():getItem().getField());
            cur = cursor;
            waitSep = true;
        }
        if(list.size()>0) {
            if(isList) return list;
            if(list.size()>7)throw new NoSearchException("TsonField support no more than 6 arguments except for the class!");
            return tsonClass.createInst(list.toArray());
        } else {
            if(isList)return tsonClass;
            return tsonClass.createInst();
        }
    }


    TsonField<?> getField(){
        return new TsonField<>(getFieldObj());
    }


    private String getKey(){
        int cur = cursor;
        b.setLength(0);
        for(char c;cur<data.length;++cur){
            if((c = data[cur]) == '=')break;
            if(c == ' ' || c == '\n')continue;
            b.append(c);
        }
        cursor = cur;
        return b.toString();
    }


    private TsonClass getTsonClass(){
        int cur = cursor;
        boolean ignore = true;
        b.setLength(0);
        for(;cur<data.length;++cur){
            char c = data[cur];
            if(ignore){
                ignore = c == '(';
                if(ignore)continue;
            }
            if(c == ' ' || c == '\n')continue;
            if(c == ')')break;
            b.append(c);
        }
        cursor = cur;
        return new TsonClass(manager, b.toString());
    }
}