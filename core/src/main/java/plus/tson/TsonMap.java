package plus.tson;

import plus.tson.security.ClassManager;
import plus.tson.utl.Te4HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * Dictionary type String -> TsonObject
 */
public class TsonMap extends Te4HashMap<String, TsonObj> implements TsonObj {
    public TsonMap(){}

    public TsonMap(int size){
        super(size);
    }


    public TsonMap(ClassManager manager, String data) {
        new TsonParser(manager, data).goTo('{').fillMap(this);
    }


    public TsonMap(String data) {
        new TsonParser(data).goTo('{').fillMap(this);
    }


    /**
     * Automatically creates a Tson wrapper before adding
     * @param v new value
     * @return previous value
     */
    public TsonObj put(String key, boolean v) {
        return super.put(key, v?TsonBool.TRUE:TsonBool.FALSE);
    }


    /**
     * Automatically creates a Tson wrapper before adding
     * @param v new value
     * @return previous value
     */
    public TsonObj put(String key, int v) {
        return super.put(key, new TsonInt(v));
    }


    /**
     * Automatically creates a Tson wrapper before adding
     * @param v new value
     * @return previous value
     */
    public TsonObj put(String key, float v) {
        return super.put(key, new TsonFloat(v));
    }


    /**
     * Automatically creates a Tson wrapper before adding
     * @param v new value
     * @return previous value
     */
    public TsonObj put(String key, double v) {
        return super.put(key, new TsonDouble(v));
    }


    /**
     * Automatically creates a Tson wrapper before adding
     * @param v new value
     * @return previous value
     */
    public TsonObj put(String key, long v){
        return super.put(key, new TsonLong(v));
    }


    /**
     * Automatically creates a Tson wrapper before adding
     * @param v new value
     * @return previous value
     */
    public TsonObj put(String key, String v) {
        return super.put(key, new TsonStr(v));
    }


    /**
     * Automatically creates a Tson wrapper before adding
     * @param v new value
     * @return previous value
     */
    public TsonObj put(String key, Object v){
        return super.put(key, new TsonField<>(v));
    }


    /**
     * Performs an action if the subject key has a value of the TsonMap type
     * @return Has the task been completed
     */
    public boolean ifContainsMap(String key, Consumer<TsonMap> task){
        TsonObj obj = get(key);
        if(obj == null)return false;
        if(obj.isMap()){
            task.accept(obj.getMap());
            return true;
        }
        return false;
    }


    /**
     * Performs an action if the subject key has a value of the TsonList type
     * @return Has the task been completed
     */
    public boolean ifContainsList(String key, Consumer<TsonList> task){
        TsonObj obj = get(key);
        if(obj == null)return false;
        if(obj.isList()){
            task.accept(obj.getList());
            return true;
        }
        return false;
    }


    /**
     * Performs an action if the subject key has a number value
     * @return Has the task been completed
     */
    public boolean ifContainsDouble(String key, Consumer<Double> task){
        TsonObj obj = get(key);
        if(obj == null)return false;
        if(obj.isNumber()){
            task.accept(obj.getDouble());
            return true;
        }
        return false;
    }


    /**
     * Performs an action if the subject key has a number value
     * @return Has the task been completed
     */
    public boolean ifContainsFloat(String key, Consumer<Float> task){
        TsonObj obj = get(key);
        if(obj == null)return false;
        if(obj.isNumber()){
            task.accept(obj.getFloat());
            return true;
        }
        return false;
    }


    /**
     * Performs an action if the subject key has a number value
     * @return Has the task been completed
     */
    public boolean ifContainsInt(String key, Consumer<Integer> task){
        TsonObj obj = get(key);
        if(obj == null)return false;
        if(obj.isNumber()){
            task.accept(obj.getInt());
            return true;
        }
        return false;
    }


    /**
     * Performs an action if the subject key has a value of the TsonBool type
     * @return Has the task been completed
     */
    public boolean ifContainsBool(String key, Consumer<Boolean> task){
        TsonObj obj = get(key);
        if(obj == null)return false;
        if(obj.isBool()){
            task.accept(obj.getBool());
            return true;
        }
        return false;
    }


    /**
     * Performs an action if the subject key has a value of the TsonStr type
     * @return Has the task been completed
     */
    public boolean ifContainsStr(String key, Consumer<String> task){
        TsonObj obj = get(key);
        if(obj == null)return false;
        if(obj.isString()){
            task.accept(obj.getStr());
            return true;
        }
        return false;
    }


    /**
     * Performs an action if the subject key has a value of the TsonFunc type
     * @return Has the task been completed
     */
    public boolean ifContainsFunc(String key, Consumer<TsonFunc> task){
        TsonObj obj = get(key);
        if(obj == null)return false;
        if(obj.isFunc()){
            task.accept((TsonFunc)obj.getField());
            return true;
        }
        return false;
    }


    protected <T> boolean ifContains0(String s, Consumer<T> c, Function<String, T> f){
        if(containsKey(s)){
            c.accept(f.apply(s));
            return true;
        }
        return false;
    }


    /**
     * Performs an action if the subject key has value
     * @return Has the task been completed
     */
    public boolean ifContains(String s, Consumer<TsonObj> c){
        return ifContains0(s, c, this::get);
    }


    @Override
    public TsonMap getMap() {
        return this;
    }


    /**
     * It is assumed that a value with this key will exist, and of type TsonBool
     * @return boolean by key
     */
    public boolean getBool(String key){
        return super.get(key).getBool();
    }


    /**
     * It is assumed that a numeric value with this key will exist
     * @return double by key
     */
    public double getDouble(String key) {
        return super.get(key).getDouble();
    }


    /**
     * It is assumed that a non-custom value with this key will exist
     * @return string by key
     */
    public String getStr(String key){
        return super.get(key).getStr();
    }


    /**
     * @return string by key, or null if not exists or cannot be represented as a string
     */
    public String getStrSafe(String key){
        TsonObj obj = super.get(key);
        if(obj == null || obj.isCustom())return null;
        return obj.getStr();
    }


    /**
     * @return string by key, or default if not exists
     * @param def default value
     */
    public String getOrDefaultStr(String key, String def) {
        TsonObj str = super.get(key);
        if(str != null && !str.isCustom())return str.getStr();
        return def;
    }


    /**
     * @return boolean by key, or default if not exists
     * @param def default value
     */
    public boolean getOrDefaultBool(String key, boolean def) {
        TsonObj bool = super.get(key);
        if(bool != null && bool.isBool())return bool.getBool();
        return def;
    }


    /**
     * @return int by key, or default if not exists
     * @param def default value
     */
    public int getOrDefaultInt(String key, int def) {
        TsonObj num = super.get(key);
        if(num != null && num.isNumber())return num.getInt();
        return def;
    }


    /**
     * @return float by key, or default if not exists
     * @param def default value
     */
    public float getOrDefaultFloat(String key, float def) {
        TsonObj num = super.get(key);
        if(num != null && num.isNumber())return num.getFloat();
        return def;
    }


    /**
     * @return double by key, or default if not exists
     * @param def default value
     */
    public double getOrDefaultDouble(String key, double def) {
        TsonObj num = super.get(key);
        if(num != null && num.isNumber())return num.getDouble();
        return def;
    }


    /**
     * It is assumed that a numeric value with this key will exist
     * @return int by key.
     */
    public int getInt(String key) {
        return super.get(key).getInt();
    }


    /**
     * It is assumed that a numeric value with this key will exist
     * @return float by key.
     */
    public float getFloat(String key) {
        return super.get(key).getFloat();
    }


    /**
     * It is assumed that a value with this key will exist, and it will be of the TsonList type
     * @return TsonList by key.
     */
    public TsonList getList(String key) {
        return super.get(key).getList();
    }


    /**
     * It is assumed that a value with this key will exist, and it will be of the TsonMap type
     * @return TsonList by key.
     */
    public TsonMap getMap(String key) {
        return super.get(key).getMap();
    }


    /**
     * @return TsonMap by key, if not exists automatically creates a TsonMap using the specified key and returns it
     */
    public TsonMap getOrCreateMap(String key){
        TsonObj result = super.get(key);
        if(result != null)return result.getMap();
        return addMap(key);
    }


    /**
     * Put an empty TsonMap to key
     * @return The added TsonMap
     */
    public TsonMap addMap(String key){
        TsonMap map = new TsonMap();
        fput(key, map);
        return map;
    }


    /**
     * Put an empty TsonList to key
     * @return The added TsonList
     */
    public TsonList addList(String key) {
        TsonList list = new TsonList();
        fput(key, list);
        return list;
    }


    @Override
    public boolean isMap(){
        return true;
    }


    @Override
    public TsonMap getField(){
        return this;
    }


    /**
     * It is assumed that value with this key will exist
     * @return Raw object by key
     */
    public Object getField(String key){
        return get(key).getField();
    }


    /**
     * It is assumed that value with this key will exist
     * @return Custom object by key and automatically cast
     */
    public <T> T getCustom(String key){
        return (T) get(key).getField();
    }


    /**
     * It is assumed that a TsonFunc with this key will exist
     * @return TsonFunc by key and automatically cast
     */
    public TsonFunc getFunc(String key){
        return (TsonFunc) get(key).getField();
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
        TJsonWriter.codeJsonObj(this, builder, ':');
    }


    @Override
    public void codeJson(StringBuilder builder){
        TJsonWriter.codeJson(this, builder, ':');
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
        TsonMap map = new TsonMap(size());
        cloneValues(map);
        return map;
    }


    protected TsonMap cloneValues(TsonMap map){
        forEach((s, obj) -> map.fput(s, obj.clone()));
        return map;
    }


    @Override
    public boolean equals(Object o) {
        return o == this;
    }


    @Override
    public Type type() {
        return Type.MAP;
    }
}