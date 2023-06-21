package plus.tson;

import plus.tson.security.ClassManager;
import plus.tson.utl.Te4HashMap;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;


public class TsonMap extends Te4HashMap<String, TsonObj> implements TsonObj {
    public TsonMap(){}


    public TsonMap(ClassManager manager, String data) {
        new TsonParser(manager, data).goTo('{').fillMap(this);
    }


    public TsonMap(String data) {
        new TsonParser(data).goTo('{').fillMap(this);
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


    public String getStrSafe(String key){
        TsonObj obj = super.get(key);
        if(obj==null)return null;
        return obj.getStr();
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
        StringBuilder builder = new StringBuilder("{");
        for(Node<String, TsonObj> node : super.table){
            if(node==null)continue;
            builder.append(node.getKey()).append('=').append(node.getValue()).append(',');
        }
        builder.setCharAt(builder.length()-1, '}');
        return builder.toString();
    }


    @Override
    public String toJsonStr() {
        StringJoiner joiner = new StringJoiner(",");
        for(String key : this.keySet()){
            joiner.add(key + ":" + super.get(key).toString());
        }
        return '{'+joiner.toString()+'}';
    }
}