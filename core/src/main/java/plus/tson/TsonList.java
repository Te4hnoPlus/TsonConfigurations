package plus.tson;

import plus.tson.security.ClassManager;
import java.util.ArrayList;
import java.util.Arrays;


public class TsonList extends ArrayList<TsonObj> implements TsonObj {
    public TsonList(int size){
        super(size);
    }


    public TsonList(){}


    public TsonList(ClassManager manager, String data) {
        new TsonParser(manager, data).goTo('[').fillList(this);
    }


    public TsonList(String data) {
        new TsonParser(data).goTo('[').fillList(this);
    }


    public TsonList(TsonObj... objects){
        this.addAll(Arrays.asList(objects));
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
        for(int i = 0;i < size(); i++){
            if(getInt(i) == n)return i;
        }
        return -1;
    }


    public int indexOf(double n){
        if(!get(0).isNumber())return -1;
        for(int i = 0;i < size();i++){
            if(getDouble(i) == n)return i;
        }
        return -1;
    }


    public int indexOf(String s){
        if(!get(0).isString())return -1;
        for(int i = 0; i < size(); i++){
            if(getStr(i).equals(s))return i;
        }
        return -1;
    }


    public boolean add(boolean b){
        return add(b?TsonBool.TRUE:TsonBool.FALSE);
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
        int size = this.size();
        if(size == 0)return "[]";
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            get(i).code(builder);
            builder.append(',');
        }
        builder.setCharAt(builder.length()-1, ']');
        return builder.toString();
    }


    @Override
    public void code(StringBuilder builder) {
        int size = this.size();
        if(size == 0){
            builder.append("[]");
        } else {
            builder.append('[');
            for (int i = 0; i < size; i++) {
                get(i).code(builder);
                builder.append(',');
            }
            builder.setCharAt(builder.length() - 1, ']');
        }
    }


    @Override
    public void codeJsonObj(StringBuilder builder) {
        int size = this.size();
        if(size==0){
            builder.append("[]");
        } else {
            builder.append('[');
            for (int i = 0; i < size; i++) {
                get(i).codeJsonObj(builder);
                builder.append(',');
            }
            builder.setCharAt(builder.length() - 1, ']');
        }
    }


    @Override
    public void codeJson(StringBuilder builder) {
        int size = this.size();
        if(size == 0){
            builder.append("[]");
        } else {
            builder.append('[');
            for (int i = 0; i < size; i++) {
                get(i).codeJsonObj(builder);
                builder.append(',');
            }
            builder.setCharAt(builder.length() - 1, ']');
        }
    }


    @Override
    public String toJsonObj() {
        int size = this.size();
        if(size==0)return "[]";
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            builder.append(get(i).toJsonObj());
            builder.append(',');
        }
        builder.setCharAt(builder.length()-1, ']');
        return builder.toString();
    }


    @Override
    public String toJsonStr() {
        int size = this.size();
        if(size == 0)return "[]";
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            builder.append(get(i).toJsonStr());
            builder.append(',');
        }
        builder.setCharAt(builder.length()-1, ']');
        return builder.toString();
    }


    @Override
    public TsonList clone() {
        int size;
        TsonList list = new TsonList(size = size());
        for(int i = 0; i < size; i++){
            list.add(this.get(i).clone());
        }
        return list;
    }


    @Override
    public TsonObj[] toArray() {
        return super.toArray(new TsonObj[size()]);
    }


    @Override
    public boolean equals(Object o) {
        return o == this;
    }


    @Override
    public Type type() {
        return Type.LIST;
    }
}