package plus.tson;

import java.util.ArrayList;
import java.util.List;


public class TsonList extends ArrayList<TsonObj> implements TsonObj {

    public TsonList(){}


    public TsonList(String data) {
        init(data);
    }


    public TsonList init(String data){
        data = data.trim();
        if(data.isEmpty())return this;

        switch (TsonObjType.scanType(data.charAt(0))) {
            case BASIC:
                for (String s : data.split(",")) {
                    add(TsonInt.build(TsonMap.getSubData(s, '(', ')')));
                }
                break;
            case STR:
                for (String s : TsonMap.split(data, '"', ',')) {
                    add(s);
                }
                break;
            case LIST:
                List<String> items = TsonMap.split(data, '[', ']', ',');
                if(items.size()==1){
                    data = items.get(0);
                    data = data.substring(1, data.length()-1);
                    return init(data);
                }
                for (String s : items) {
                    add(new TsonList(s.substring(1, s.length()-1)));
                }
                break;
            case MAP:
                for (String s : TsonMap.split(data, '{', '}', ',')) {
                    add(new TsonMap(s));
                }
                break;
            case FIELD:
                for (String s : TsonMap.split(data, '<', '>', ',')) {
                    add(TsonField.build(s));
                }
                break;
        }
        return this;
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
        String[] strings = new String[this.size()];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = get(i).toString();
        }
        return '[' + String.join(", ", strings) + ']';
    }
}