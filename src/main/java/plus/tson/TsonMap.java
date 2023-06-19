package plus.tson;

import plus.tson.exception.NoSearchException;
import plus.tson.security.ClassManager;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;


public class TsonMap extends HashMap<String, TsonObj> implements TsonObj {
    public TsonMap(){}


    public TsonMap(String data) {
        this(new ClassManager.Def(), data);
    }


    public TsonMap(ClassManager manager, String data) {
        init(manager, data);
    }


    protected final TsonMap init(ClassManager manager, String data){
        data = data.trim();
        if (data.isEmpty()) return this;
        try {
            getSubStrBefore(getSubStrBefore(data, "="), "{");
            data = data.substring(1, data.length() - 1).trim();
            if (data.isEmpty()) return this;
        } catch (NoSearchException ignored) {}
        for (String raw : splitStr(data)) {
            processItem(manager, raw);
        }
        return this;
    }


    private void processItem(ClassManager manager, String raw){
        try {
            String key = getSubStrBefore(raw, "=").trim();
            switch (TsonObjType.scanType(raw)) {
                case STR:
                    put(key, getSubData(raw, '"'));
                    break;
                case MAP:
                    put(key, new TsonMap(manager, getSubData(raw, '{', '}')));
                    break;
                case LIST:
                    put(key, new TsonList(manager, getSubData(raw, '[', ']')));
                    break;
                case BASIC:
                    put(key, TsonPrimitive.build(manager, getSubData(raw, '(', ')')));
                    break;
                case FIELD:
                    put(key, TsonField.build(manager, getSubData(raw, '<', '>')));
                    break;
            }
        } catch (NoSearchException ignored) {}
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


    public boolean ifContainsMap(String s, Consumer<TsonMap> c){
        return ifContains(s, c, this::getMap);
    }


    public boolean ifContainsList(String s, Consumer<TsonList> c){
        return ifContains(s, c, this::getList);
    }


    public boolean ifContainsDouble(String s, Consumer<Double> c){
        return ifContains(s, c, this::getDouble);
    }


    public boolean ifContainsFloat(String s, Consumer<Float> c){
        return ifContains(s, c, this::getFloat);
    }


    public boolean ifContainsInt(String s, Consumer<Integer> c){
        return ifContains(s, c, this::getInt);
    }


    public boolean ifContainsBool(String s, Consumer<Boolean> c){
        return ifContains(s, c, this::getBool);
    }


    public boolean ifContainsStr(String s, Consumer<String> c){
        return ifContains(s, c, this::getStr);
    }


    public<T> boolean ifContains(String s, Consumer<T> c, Function<String, T> f){
        if(containsKey(s)){
            c.accept(f.apply(s));
            return true;
        }
        return false;
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
        StringJoiner joiner = new StringJoiner(",");
        for(String key : this.keySet()){
            joiner.add(key + "=" + super.get(key).toString());
        }
        return '{'+joiner.toString()+'}';
    }


    @Override
    public String toJsonStr() {
        StringJoiner joiner = new StringJoiner(",");
        for(String key : this.keySet()){
            joiner.add(key + ":" + super.get(key).toString());
        }
        return '{'+joiner.toString()+'}';
    }


    private static final char[] startSeparators = new char[]{'[', '{', '(', '<'};
    private static final char[] endSeparators = new char[]{']', '}', ')', '>'};
    private static final char objectSeparator = ',';

    static{
        Arrays.sort(startSeparators);
        Arrays.sort(endSeparators);
    }


    protected static boolean contains(char[] chars, char key){
        for(char check:chars){
            if(check == key)return true;
        }
        return false;
    }


    protected static List<String> splitStr(String data){
        data = data.trim();

        int openned = contains(startSeparators, data.charAt(0))?1:0;
        int closed = 0;

        List<String> list = new ArrayList<>(3);
        StringBuilder buffer = new StringBuilder(10);
        boolean waitSep = false;
        boolean waitStart = true;
        boolean waitEndStr = false;

        for(char c:data.toCharArray()){
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
                if(c==objectSeparator){
                    list.add(buffer.toString().trim());
                    buffer = new StringBuilder(10);
                    waitSep = false;
                } else {
                    buffer.append(c);
                }
            } else {
                buffer.append(c);
            }

            boolean cM1 = contains(startSeparators, c);

            if(waitStart && cM1){
                continue;
            } else {
                waitStart = false;
            }
            if(cM1){
                ++openned;
            }else if(contains(endSeparators, c)){
                ++closed;
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
        data = data.trim();

        int start = data.indexOf(m1);
        if(start==-1)throw new NoSearchException();
        int end = data.indexOf(m2, start);
        if(end==-1)throw new NoSearchException();

        short opened = 0;
        short closed = 0;
        int ln = data.length();

        for(int i=start;i<ln;i++){
            char c = data.charAt(i);
            if(c==m1){
                ++opened;
            }
            if(c==m2){
                ++closed;
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