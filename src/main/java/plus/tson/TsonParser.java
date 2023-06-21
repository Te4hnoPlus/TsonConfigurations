package plus.tson;

import plus.tson.exception.NoSearchException;
import plus.tson.security.ClassManager;


class TsonParser {
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


    protected TsonParser goToFirst(){
        int cur = cursor;
        for(;cur<data.length;++cur){
            char c = data[cur];
            if(!(c==' ' || c == '\n'))break;
        }
        cursor = cur;
        return this;
    }


    protected TsonParser goTo(char chr){
        int cur = cursor;
        for(;cur<data.length;++cur){
            if(data[cur]==chr)break;
        }
        cursor = cur+1;
        return this;
    }


    protected TsonObj getItem(){
        switch (data[cursor++]){
            case '(': return getBasic();
            case '"':
                return getStr('"');
            case '\'':
                return getStr('\'');
            case '{':
                return getMap();
            case '[':
                return getList();
            case '<':
                return getField();
        }
        throw new NoSearchException("Char ["+data[cursor]+"] not supported");
    }


    private TsonMap getMap(){
        TsonMap map = new TsonMap();
        fillMap(map);
        return map;
    }


    protected void fillMap(TsonMap map){
        int cur = cursor;
        boolean waitSep = false;
        boolean waitKey = true;
        String key = null;
        for(;cur<data.length;++cur){
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


    protected TsonList getList(){
        TsonList list = new TsonList();
        fillList(list);
        return list;
    }


    protected void fillList(TsonList list){
        int cur = cursor;
        boolean first = true;
        boolean waitSep = true;
        for(;cur<data.length;++cur){
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
        int cur = cursor;
        boolean prevEcran = false;
        StringBuilder b = new StringBuilder();
        for(;cur<data.length;++cur){
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


    protected TsonPrimitive getBasic(){
        int cur = cursor;
        StringBuilder b = new StringBuilder();
        for(;cur<data.length;++cur){
            char c = data[cur];
            if(c==' ' || c == '\n')continue;
            if(c==')')break;
            b.append(c);
        }
        cursor = cur;
        return getBasicOfStr(b.toString());
    }


    protected TsonField<?> getField(){
        TsonClass tsonClass = getTsonClass();
        TsonList list = new TsonList();
        int cur = cursor;
        boolean waitSep = true;
        for(;cur<data.length;++cur){
            char c = data[cur];
            if(c == '>') break;
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
        if(list.size()>0) {
            if(list.size()>7)throw new NoSearchException("TsonField support no more than 6 arguments except for the class!");
            return new TsonField<>(tsonClass.createInst(list.toArray()));
        } else {
            return new TsonField<>(tsonClass.createInst());
        }
    }


    private String getKey(){
        int cur = cursor;
        StringBuilder b = new StringBuilder();
        for(;cur<data.length;++cur){
            char c = data[cur];
            if(c==' ' || c == '\n')continue;
            if(c=='=')break;
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


    private TsonPrimitive getBasicOfStr(String value) {
        if(value.equalsIgnoreCase("true")){
            return new TsonBool(true);
        } else if(value.equalsIgnoreCase("false")){
            return new TsonBool(false);
        }
        try {
            if (value.contains(".")) {
                if(value.length()>7){
                    return new TsonDouble(Double.parseDouble(value));
                } else {
                    return new TsonFloat(Float.parseFloat(value));
                }
            } else return new TsonInt(Integer.parseInt(value));
        } catch (NumberFormatException e){
            return new TsonClass(manager, value);
        }
    }
}