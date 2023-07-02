package plus.tson;

import plus.tson.security.ClassManager;
import java.util.ArrayList;
import java.util.StringJoiner;


public class TsonList extends ArrayList<TsonObj> implements TsonObj {

    public TsonList(){}


    public TsonList(ClassManager manager, String data) {
        new TsonParser(manager, data).goTo('[').fillList(this);
    }


    public TsonList(String data) {
        new TsonParser(data).goTo('[').fillList(this);
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
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < this.size(); i++) {
            get(i).code(builder);
            builder.append(',');
        }
        builder.setCharAt(builder.length()-1, ']');
        return builder.toString();
    }


    @Override
    public void code(StringBuilder builder) {
        builder.append('[');
        for (int i = 0; i < this.size(); i++) {
            get(i).code(builder);
            builder.append(',');
        }
        builder.setCharAt(builder.length()-1, ']');
    }


    @Override
    public String toJsonStr() {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < this.size(); i++) {
            builder.append(get(i).toJsonStr());
            builder.append(',');
        }
        builder.setCharAt(builder.length()-1, ']');
        return builder.toString();
    }
}