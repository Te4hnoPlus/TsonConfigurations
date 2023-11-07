package plus.tson;

import plus.tson.security.ClassManager;
import plus.tson.utl.Te4HashMap;
import java.util.Map;
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
        return (TsonBool) super.put(key, v?TsonBool.TRUE:TsonBool.FALSE);
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
        return ifContains0(s, c, this::getMap);
    }


    public boolean ifContainsList(String s, Consumer<TsonList> c){
        return ifContains0(s, c, this::getList);
    }


    public boolean ifContainsDouble(String s, Consumer<Double> c){
        return ifContains0(s, c, this::getDouble);
    }


    public boolean ifContainsFloat(String s, Consumer<Float> c){
        return ifContains0(s, c, this::getFloat);
    }


    public boolean ifContainsInt(String s, Consumer<Integer> c){
        return ifContains0(s, c, this::getInt);
    }


    public boolean ifContainsBool(String s, Consumer<Boolean> c){
        return ifContains0(s, c, this::getBool);
    }


    public boolean ifContainsStr(String s, Consumer<String> c){
        return ifContains0(s, c, this::getStr);
    }


    protected <T> boolean ifContains0(String s, Consumer<T> c, Function<String, T> f){
        if(containsKey(s)){
            c.accept(f.apply(s));
            return true;
        }
        return false;
    }


    public boolean ifContains(String s, Consumer<TsonObj> c){
        return ifContains0(s, c, this::get);
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


    public String getOrDefaultStr(String key, String def) {
        TsonObj str = super.get(key);
        if(str != null && str.isString())return str.getStr();
        return def;
    }


    public boolean getOrDefaultBool(String key, boolean def) {
        TsonObj bool = super.get(key);
        if(bool != null && bool.isBool())return bool.getBool();
        return def;
    }


    public int getOrDefaultInt(String key, int def) {
        TsonObj num = super.get(key);
        if(num != null && num.isNumber())return num.getInt();
        return def;
    }


    public float getOrDefaultFloat(String key, float def) {
        TsonObj num = super.get(key);
        if(num != null && num.isNumber())return num.getFloat();
        return def;
    }


    public double getOrDefaultDouble(String key, double def) {
        TsonObj num = super.get(key);
        if(num != null && num.isNumber())return num.getDouble();
        return def;
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


    public TsonMap getOrCreateMap(String key){
        TsonObj result = super.get(key);
        if(result != null)return result.getMap();
        return addMap(key);
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
        if(super.isEmpty())return "{}";
        StringBuilder builder = new StringBuilder("{");
        for (Map.Entry<String, TsonObj> node : super.entrySet()) {
            builder.append(node.getKey()).append('=');
            node.getValue().code(builder);
            builder.append(',');
        }
        builder.setCharAt(builder.length() - 1, '}');
        return builder.toString();
    }


    @Override
    public void code(StringBuilder builder) {
        if(super.size()==0){
            builder.append("{}");
        } else {
            builder.append('{');
            for (Map.Entry<String, TsonObj> node : super.entrySet()) {
                builder.append(node.getKey()).append('=');
                node.getValue().code(builder);
                builder.append(',');
            }
            builder.setCharAt(builder.length() - 1, '}');
        }
    }


    @Override
    public void codeJsonObj(StringBuilder builder) {
        if(super.size()==0){
            builder.append("{}");
        } else {
            builder.append('{');
            for (Map.Entry<String, TsonObj> node : super.entrySet()) {
                builder.append(node.getKey()).append(':');
                node.getValue().codeJsonObj(builder);
                builder.append(',');
            }
            builder.setCharAt(builder.length() - 1, '}');
        }
    }


    @Override
    public void codeJson(StringBuilder builder){
        if(super.size()==0){
            builder.append("{}");
        } else {
            builder.append('{');
            for (Map.Entry<String, TsonObj> node : super.entrySet()) {
                builder.append('"').append(node.getKey()).append("\":");
                node.getValue().codeJson(builder);
                builder.append(',');
            }
            builder.setCharAt(builder.length() - 1, '}');
        }
    }


    @Override
    public String toJsonObj() {
        if (super.isEmpty()) return "{}";
        StringBuilder builder = new StringBuilder("{");
        for (Map.Entry<String, TsonObj> node : super.entrySet()) {
            builder.append(node.getKey()).append(':');
            node.getValue().codeJsonObj(builder);
            builder.append(',');
        }
        builder.setCharAt(builder.length() - 1, '}');
        return builder.toString();
    }


    @Override
    public String toJsonStr() {
        if(super.isEmpty()) return "{}";
        StringBuilder builder = new StringBuilder("{");
        for (Map.Entry<String, TsonObj> node : super.entrySet()) {
            builder.append('"').append(node.getKey()).append("\":");
            node.getValue().codeJson(builder);
            builder.append(',');
        }
        builder.setCharAt(builder.length() - 1, '}');
        return builder.toString();
    }


    @Override
    public TsonMap clone() {
        return (TsonMap) super.clone();
    }


    @Override
    public boolean equals(Object o) {
        return o == this;
    }
}