package plus.tson;

import plus.tson.security.ClassManager;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;


public class TsonList extends ArrayList<TsonObj> implements TsonObj {

    public TsonList(){}


    public TsonList(String data) {
        this(new ClassManager.Def(), data);
    }


    public TsonList(ClassManager manager, String data) {
        init(manager, data);
    }


    private TsonList init(ClassManager manager, String data){
        data = data.trim();
        if(data.isEmpty())return this;

        switch (TsonObjType.scanType(data.charAt(0))) {
            case BASIC:
                for (String s : data.split(",")) {
                    add(TsonInt.build(TsonMap.getSubData(s, '(', ')')));
                }
                break;
            case STR:
                addStrList(data);
                break;
            case LIST:
                List<String> items = splitStr(data, '[', ']');
                if(items.size()==1){
                    data = items.get(0);
                    data = data.substring(1, data.length()-1);
                    return init(manager, data);
                }
                for (String s : items) {
                    add(new TsonList(manager, s.substring(1, s.length()-1)));
                }
                break;
            case MAP:
                for (String s : splitStr(data, '{', '}')) {
                    add(new TsonMap(s));
                }
                break;
            case FIELD:
                for (String s : splitStr(data, '<', '>')) {
                    add(TsonField.build(manager, s));
                }
                break;
        }
        return this;
    }


    private static List<String> splitStr(String data, char m1, char m2){
        data = data.trim();
        int openned = 0;
        int closed = 0;

        List<String> list = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
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
                if(c== ','){
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
                ++openned;
            }else if(c==m2){
                if(openned==++closed){
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


    private void addStrList(String data){
        StringBuilder buffer = new StringBuilder(10);
        boolean waitStart = true;
        boolean waitEnd = false;

        for(char c:data.toCharArray()){
            if(waitStart){
                if(c== '"'){
                    waitStart = false;
                    waitEnd = true;
                }
            } else if(waitEnd){
                if(c== '"'){
                    waitEnd = false;
                    add(buffer.toString());
                    buffer = new StringBuilder(10);
                } else {
                    buffer.append(c);
                }
            } else {
                if(c== ','){
                    waitStart = true;
                }
            }
        }
    }


    public int getInt(int i){
        return get(i).getInt();
    }


    public float getFloat(int i) {
        return get(i).getFloat();
    }


    public String getStr(int i){
        return get(i).getStr();
    }


    public double getDouble(int i){
        return get(i).getDouble();
    }


    public boolean getBool(int i) {
        return get(i).getBool();
    }


    public TsonList getList(int i){
        return get(i).getList();
    }


    public TsonMap getMap(int i){
        return get(i).getMap();
    }


    public boolean contains(String s){
        return indexOf(s)>-1;
    }


    public boolean contains(int n){
        return indexOf(n)>-1;
    }


    public boolean contains(double n){
        return indexOf(n)>-1;
    }


    public int indexOf(int n){
        if(!get(0).isNumber())return -1;
        for(int i=0;i<size();i++){
            if(getInt(i)==n)return i;
        }
        return -1;
    }


    public int indexOf(double n){
        if(!get(0).isNumber())return -1;
        for(int i=0;i<size();i++){
            if(getDouble(i)==n)return i;
        }
        return -1;
    }


    public int indexOf(String s){
        if(!get(0).isString())return -1;
        for(int i=0;i<size();i++){
            if(getStr(i).equals(s))return i;
        }
        return -1;
    }


    public boolean add(boolean n){
        return add(new TsonBool(n));
    }


    public boolean add(int n){
        return add(new TsonInt(n));
    }


    public boolean add(float n){
        return add(new TsonFloat(n));
    }


    public boolean add(double n) {
        return add(new TsonDouble(n));
    }


    public boolean add(String s) {
        return add(new TsonStr(s));
    }


    public boolean add(TsonSerelizable o){
        return add(new TsonField<>(o));
    }


    @Override
    public boolean isList(){
        return true;
    }


    @Override
    public TsonList getList(){
        return this;
    }


    @Override
    public TsonList getField(){
        return this;
    }


    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",");
        for (int i = 0; i < this.size(); i++) {
            joiner.add(get(i).toString());
        }
        return '[' + joiner.toString() + ']';
    }
}