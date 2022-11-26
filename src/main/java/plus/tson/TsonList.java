package plus.tson;

import java.util.ArrayList;
import static plus.tson.TsonMap.gen;


public class TsonList extends ArrayList<TsonObj> implements TsonObj {

    public TsonList(){}


    public TsonList(String data) {
        data = data.trim();
        if(data.equals(""))return;

        switch (TsonObjType.scanType(data.charAt(0))) {
            case NUMBER:
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
                for (String s : TsonMap.split(data, '[', ']', ',')) {
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

                    add(new TsonField<>(gen(s.substring(1, s.length()-1))));
                }
                break;
        }
    }


    public boolean add(double n) {
        return add(new TsonDouble(n));
    }


    public boolean add(int n){
        return add(new TsonInt(n));
    }


    public boolean add(String s) {
        return add(new TsonStr(s));
    }


    public boolean add(TsonSerelizable o){
        return add(new TsonField<>(o));
    }


    @Override
    public boolean isList(){return true;}


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