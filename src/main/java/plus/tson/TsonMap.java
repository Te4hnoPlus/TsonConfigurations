package plus.tson;

import plus.tson.exception.NoSearchException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TsonMap extends HashMap<String, TsonObj> implements TsonObj {
    public TsonMap(){}


    public TsonMap(String data) {
        init(data);
    }


    protected TsonMap init(String data){
        data = data.trim();
        if (data.isEmpty()) return this;

        try {
            getSubStrBefore(getSubStrBefore(data, "="), "{");
            data = data.substring(1, data.length() - 1).trim();
            if (data.isEmpty()) return this;
        } catch (NoSearchException ignored) {}

        for (String raw : split(data, startSeparators, endSeparators, objectSeparator)) {
            try {
                String key = getSubStrBefore(raw, "=").trim();
                switch (TsonObjType.scanType(raw)) {
                    case STR:
                        put(key, getSubData(raw, '"'));
                        break;
                    case MAP:
                        put(key, new TsonMap(
                                getSubData(raw, '{', '}')
                        ));
                        break;
                    case LIST:
                        put(key, new TsonList(
                                getSubData(raw, '[', ']')
                        ));
                        break;
                    case BASIC:
                        put(key, TsonPrimitive.build(getSubData(raw, '(', ')')));
                        break;
                    case FIELD:
                        put(key, TsonField.build(getSubData(raw, '<', '>')));
                }
            } catch (NoSearchException e) {
                System.out.println(e.getStackTrace()[1].getLineNumber()+ " "+ e.getMessage());
            }
        }
        return this;
    }


    public TsonBool put(String key, boolean v) {
        return (TsonBool) super.put(key, new TsonBool(v));
    }


    public TsonInt put(String key, int v) {
        return (TsonInt) super.put(key, new TsonInt(v));
    }


    public TsonFloat put(String key, float v) {
        return (TsonFloat) super.put(key, new TsonFloat(v));
    }


    public TsonDouble put(String key, double v) {
        return (TsonDouble) super.put(key, new TsonDouble(v));
    }


    public TsonStr put(String key, String v) {
        return (TsonStr) super.put(key, new TsonStr(v));
    }


    public <T extends TsonSerelizable> TsonField put(String key, T v){
        return (TsonField<T>) super.put(key, new TsonField<>(v));
    }


    public TsonList addList(String key) {
        TsonList list = new TsonList();
        super.put(key, list);
        return list;
    }


    @Override
    public TsonMap getMap() {
        return this;
    }


    public boolean getBool(String key){
        return super.get(key).getBool();
    }


    public double getDouble(String key) {
        return super.get(key).getDouble();
    }


    public String getStr(String key){
        return super.get(key).getStr();
    }


    public int getInt(String key) {
        return super.get(key).getInt();
    }


    public float getFloat(String key) {
        return super.get(key).getFloat();
    }


    public TsonList getList(String key) {
        return super.get(key).getList();
    }


    public TsonMap getMap(String key) {
        return super.get(key).getMap();
    }


    public TsonMap addMap(String key){
        TsonMap map = new TsonMap();
        put(key, map);
        return map;
    }


    @Override
    public boolean isMap(){
        return true;
    }


    @Override
    public TsonMap getField(){
        return this;
    }


    public Object getField(String key){
        return get(key).getField();
    }


    @Override
    public String toString() {
        String[] strings = new String[this.size()];
        int i = 0;
        for (String key : this.keySet()) {
            strings[i] = key + "=" + super.get(key).toString();
            ++i;
        }
        return '{' + String.join(", ", strings) + '}';
    }


    protected static final char[] startSeparators = new char[]{'[', '{', '(', '<'};
    protected static final char[] endSeparators = new char[]{']', '}', ')', '>'};
    protected static final char objectSeparator = ',';

    protected static List<String> split(String data, char m, char sep){
        List<String> list = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        boolean waitStart = true;
        boolean waitEnd = false;

        for(int i=0;i<data.length();i++){
            char c = data.charAt(i);
            if(waitStart){
                if(c==m){
                    waitStart = false;
                    waitEnd = true;
                }
            } else if(waitEnd){
                if(c==m){
                    waitEnd = false;
                    list.add(buffer.toString());
                    buffer = new StringBuilder();
                } else {
                    buffer.append(c);
                }
            } else {
                if(c==sep){
                    waitStart = true;
                }
            }
        }

        return list;
    }


    protected static boolean contains(char[] chars, char c){
        for(char check:chars){
            if(check == c)return true;
        }
        return false;
    }


    protected static List<String> split(String data, char[] m1, char[] m2, char sep){
        data = data.trim();

        int openned = contains(m1, data.charAt(0))?1:0;
        int closed = 0;

        List<String> list = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        boolean waitSep = false;
        boolean waitStart = true;
        boolean waitEndStr = false;

        for(int i=0;i<data.length();i++){
            char c = data.charAt(i);

            if (c == '"') {
                if(waitEndStr){
                    if(openned == closed){
                        waitSep = true;
                    }
                }
                waitEndStr = !waitEndStr;
                buffer.append(c);
                continue;
            }
            if(waitEndStr){
                buffer.append(c);
                continue;
            }

            if(waitSep){
                if(c==sep){
                    list.add(buffer.toString().trim());
                    buffer = new StringBuilder();
                    waitSep = false;
                } else {
                    buffer.append(c);
                }
            } else {
                buffer.append(c);
            }

            boolean cM1 = contains(m1, c);

            if(waitStart && cM1){
                continue;
            } else {
                waitStart = false;
            }
            if(cM1){
                openned+=1;
            }else if(contains(m2, c)){
                closed+=1;
                if(openned==closed){
                    waitStart = true;
                    waitSep = true;
                }
            }
        }

        if(openned != closed){
            throw new RuntimeException("Json syntax error! --> "+data);
        }
        String result = buffer.toString().trim();
        if(!result.isEmpty()){
            list.add(result);
        }
        return list;
    }


    protected static List<String> split(String data, char m1, char m2, char sep){
        if(m1==m2){
            return split(data, m1, sep);
        }
        data = data.trim();
        int openned = 0;
        int closed = 0;

        List<String> list = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        boolean waitSep = false;
        boolean waitStart = true;

        for(int i=0;i<data.length();i++){
            char c = data.charAt(i);
            if(waitSep){
                if(c==sep){
                    list.add(buffer.toString().trim());
                    buffer = new StringBuilder();
                    waitSep = false;
                } else {
                    buffer.append(c);
                }
                continue;
            } else {
                buffer.append(c);
            }

            if(waitStart && c != m1){
                continue;
            } else {
                waitStart = false;
            }
            if(c==m1){
                openned+=1;
            }else if(c==m2){
                closed+=1;
                if(openned==closed){
                    waitStart = true;
                    waitSep = true;
                }
            }
        }
        if(openned != closed){
            throw new RuntimeException("Tson syntax error!");
        }
        String result = buffer.toString().trim();
        if(!result.equals("")){
            list.add(result);
        }
        return list;
    }


    public static String getSubData(String from, String search, char m1, char m2){
        int index = from.indexOf(search+" ");
        if(index==-1){
            index = from.indexOf(search+"=");
            if(index==-1)return null;
        }
        for(int i = index+search.length();i<from.length();i++){
            char c = from.charAt(i);
            if(c=='='){
                return getSubData(from.substring(i), m1, m2);
            } else if(c==m1){
                return null;
            }
        }
        throw new NoSearchException(search+" in "+from);
    }


    public static String getSubData(String data, int start, char m1, char m2){
        return getSubData(data.substring(start), m1, m2);
    }


    public static String getSubData(String data, char m){
        StringBuilder buffer = new StringBuilder();
        boolean scanning = false;
        for(int i=0;i<data.length();i++){
            char c = data.charAt(i);
            if(scanning){
                if(c==m){
                    return buffer.toString();
                } else {
                    buffer.append(c);
                }
            } else {
                if(c==m)scanning = true;
            }
        }
        return null;
    }


    public static String getSubData(String data, char m1, char m2){
        if(m1==m2)return getSubData(data, m1);
        data = data.trim();

        int start = data.indexOf(m1);
        if(start==-1)throw new NoSearchException();
        int end = data.indexOf(m2, start);
        if(end==-1)throw new NoSearchException();

        int opened = 0;
        int closed = 0;

        for(int i=start;i<data.length();i++){
            char c = data.charAt(i);
            if(c==m1){
                opened+=1;
            }
            if(c==m2){
                closed+=1;
            }
            if(opened==closed){
                end = i;
                break;
            }
        }
        return data.substring(start+1, end);
    }


    protected static String getSubStrAfter(String s, String c) {
        int index = s.indexOf(c);
        if (index == -1) throw new NoSearchException();
        return s.substring(index + 1);
    }


    protected static String getSubStrBefore(String s, String c) {
        int index = s.indexOf(c);
        if (index == -1) throw new NoSearchException();
        return s.substring(0, index);
    }
}