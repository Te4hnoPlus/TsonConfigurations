package plus.tson;

import plus.tson.exception.NoSearchException;
import plus.tson.exception.TsonSyntaxException;
import plus.tson.security.ClassManager;
import java.util.ArrayList;


final class TsonParser {
    private final ClassManager manager;
    private final char[] data;
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
        int cur;
        for(cur=cursor;cur<data.length;++cur){
            char c = data[cur];
            if(!(c==' ' || c == '\n'))break;
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
            case '"': return getStr('"');
            case '\'': return getStr('\'');
            case '(': return getBasic();
            case '{': return getMap();
            case '[': return getList();
            case '<': return getField();
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
        int cur;
        boolean waitSep = false;
        boolean waitKey = true;
        String key = null;
        for(cur = cursor;cur<data.length;++cur){
            char c = data[cur];
            if(c=='}')break;
            if(waitSep){
                if(c==',')waitSep = false;
                continue;
            }
            if(c==' ' || c == '\n')continue;
            cursor = cur;
            if(waitKey) {
                waitKey = false;
                key = getKey();
            } else {
                map.put(key, getItem());
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
        boolean first = true;
        boolean waitSep = true;
        for(int cur=cursor;cur<data.length;++cur){
            char c = data[cur];
            if(c == ']') break;
            if(first){
                if(c==' ' || c == '\n')continue;
                cursor = cur;
                list.add(getItem());
                cur = cursor;
                first = false;
            } else {
                if(waitSep){
                    if(c==',')waitSep = false;
                    continue;
                }
                if(c==' ' || c == '\n')continue;
                cursor = cur;
                list.add(getItem());
                cur = cursor;
                waitSep = true;
            }
        }
    }


    private TsonStr getStr(char end){
        int cur;
        boolean prevEcran = false;
        StringBuilder b = new StringBuilder();
        for(cur=cursor;cur<data.length;++cur){
            char c = data[cur];
            if(c==end && !prevEcran)break;
            if(c=='\\'){
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
        int cur;
        for(cur=cursor;cur<data.length;++cur){
            char c = data[cur];
            if(c==' ' || c == '\n')continue;
            if(c > 47 && c < 58 || c == '-'){
                cursor = cur;
                return readNum();
            }
            if(c==')')break;
            else {
                cursor = cur;
                return readClassOrBool();
            }
        }
        throw new TsonSyntaxException(getErrorString(), cur, data[cur]);
    }


    private TsonPrimitive readClassOrBool(){
        int cur;
        StringBuilder b = new StringBuilder();
        for(cur=cursor;cur<data.length;++cur){
            char c = data[cur];
            if(c==')')break;
            b.append(c);
        }
        cursor = cur;
        String s = b.toString();
        if(s.equalsIgnoreCase("true"))return TsonBool.TRUE;
        if(s.equalsIgnoreCase("false"))return TsonBool.FALSE;
        return new TsonClass(manager, s);
    }


    private TsonPrimitive readNum(){
        int cur, num, size = 0;
        boolean invert;
        if(invert = (data[cursor] == '-')) ++cursor;
        num = data[cursor]-48;

        boolean dec = false;

        for(cur=cursor+1;cur<data.length;++cur){
            char c = data[cur];
            if(c>47 && c < 58) {
                num = num * 10 + (c-48);
                ++size;
            } else if(c=='.'){
                dec = true;
                ++cur;
                break;
            }
            else if(c == ')') break;
            else if(c!='_') throw new TsonSyntaxException(getErrorString(), cursor = cur, c);
        }
        if(dec){
            double num2 = num;
            int dec1 = invert?-1:1;
            for(;cur<data.length;++cur){
                char c = data[cur];
                if(c>47 && c < 58) {
                    num2 = num2 * 10 + (c-48);
                    dec1 *= 10;
                    ++size;
                }
                else if(c == ')') break;
                else if(c!='_') throw new TsonSyntaxException(getErrorString(), cursor = cur, c);
            }
            if(size>6){
                return new TsonDouble(num2/dec1);
            } else {
                return new TsonFloat((float) (num2/dec1));
            }
        } else {
            cursor = cur;
            return invert?new TsonInt(-num):new TsonInt(num);
        }
    }


    Object getFieldObj(){
        TsonClass tsonClass = getTsonClass();
        ArrayList<Object> list = new ArrayList<>();
        boolean waitSep = true;
        for(int cur = cursor;cur<data.length;++cur){
            char c = data[cur];
            if(c == '>') break;
            if(waitSep){
                if(c==',')waitSep = false;
                continue;
            }
            if(c==' ' || c == '\n')continue;
            cursor = cur;
            list.add(getItem().getField());
            cur = cursor;
            waitSep = true;
        }
        if(list.size()>0) {
            if(list.size()>7)throw new NoSearchException("TsonField support no more than 6 arguments except for the class!");
            return tsonClass.createInst(list.toArray());
        } else {
            return tsonClass.createInst();
        }
    }


    TsonField<?> getField(){
        return new TsonField<>(getFieldObj());
    }


    private String getKey(){
        int cur = cursor;
        StringBuilder b = new StringBuilder();
        for(;cur<data.length;++cur){
            char c = data[cur];
            if(c=='=')break;
            if(c==' ' || c == '\n')continue;
            b.append(c);
        }
        cursor = cur;
        return b.toString();
    }


    private TsonClass getTsonClass(){
        int cur = cursor;
        boolean ignore = true;
        StringBuilder b = new StringBuilder();
        for(;cur<data.length;++cur){
            char c = data[cur];
            if(ignore){
                ignore = c=='(';
                if(ignore)continue;
            }
            if(c==' ' || c == '\n')continue;
            if(c==')')break;
            b.append(c);
        }
        cursor = cur;
        return new TsonClass(manager, b.toString());
    }
}