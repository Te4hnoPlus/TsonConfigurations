package plus.tson;

import plus.tson.exception.WrongTypeException;


/**
 * The main interface of TsonConfigurations
 * <br>
 * It is intended for generalization and convenient storage of objects of indeterminate type
 * <br><br>
 * Usage examples:
 * <br><br>
 * TsonMap:
 * <pre>{@code
 * String tsonCode = "{key1 = (10_000), key2 = 'value'}";
 *
 * TsonMap map1 = TsonObj.ofString(tsonCode).getMap();
 * TsonMap map2 = new TsonMap(tsonCode);
 * }
 * </pre>
 * TsonList:
 * <pre>{@code
 * String tsonCodeList = "[(3_000.1_250), (true), (java.lang.String)]";
 *
 * TsonList list1 = TsonObj.ofString(tsonCodeList).getList();
 * TsonList list1 = new TsonList(tsonCodeList);
 * }
 * </pre>
 * Check Types:
 * <pre>{@code
 * TsonObj obj = ...
 * if(obj.isNumber()){
 *     int num = obj.getInt()
 *     //do something 1
 * } else if(obj.isString()){
 *     String str = obj.getStr()
 *     //do something 2
 * }
 * }
 * </pre>
 * Custom:
 * <pre>{@code
 *class Example implements TsonSerelizable{
 *     private final String k;
 *     private final String v;
 *     public Example(String k, String v){
 *         this.k = k;this.v = v;
 *     }
 *
 *     public Example(TsonMap mp){
 *         k = mp.getStr("k");
 *         v = mp.getStr("v");
 *     }
 *
 *     @Override
 *     public TsonObj toTson() {
 *         TsonMap mp = new TsonMap();
 *         mp.put("k", k);
 *         mp.put("v",v);
 *         return mp;
 *     }
 * }
 *
 * //and then
 * TsonField<Example> filed = new TsonField<>(
 *         "'<(Example), 'k1', 'v1'>'"
 * );
 * Example exm = field.getFiled();
 * }
 * </pre>
 *
 * <br><br>
 */
public interface TsonObj extends Cloneable{
    /**
     * Parse incoming string
     * @param str A Tson String
     */
    static TsonObj ofString(String str){
        return new TsonParser(str).getAutho();
    }


    /**
     * Supported by all TsonObject implementations
     * @return Value represented as a string
     */
    default String getStr() {
        throw new WrongTypeException(this.getClass(),"getStr()");
    }


    /**
     * Supported by all TsonPrimitive implementations
     * @return Value represented as a int
     */
    default int getInt() {
        throw new WrongTypeException(this.getClass(),"getInt()");
    }


    /**
     * Supported by all TsonPrimitive implementations
     * @return Value represented as a double
     */
    default double getDouble() {
        throw new WrongTypeException(this.getClass(),"getDouble()");
    }


    /**
     * Supported by all TsonPrimitive implementations
     * @return Value represented as a long
     */
    default long getLong(){
        throw new WrongTypeException(this.getClass(),"getLong()");
    }


    /**
     * Supported by all TsonPrimitive implementations
     * @return Value represented as a float
     */
    default float getFloat(){
        throw new WrongTypeException(this.getClass(),"getFloat()");
    }


    /**
     * Supported by all TsonPrimitive implementations
     * @return Value represented as a boolean
     */
    default boolean getBool(){
        throw new WrongTypeException(this.getClass(),"getBool()");
    }


    /**
     * @return Value represented as a TsonMap
     */
    default TsonMap getMap() {
        throw new WrongTypeException(this.getClass(),"getMap()");
    }


    /**
     * @return Value represented as a TsonList
     */
    default TsonList getList() {
        throw new WrongTypeException(this.getClass(),"getList()");
    }


    /**
     * @return Returns the native value of the content
     */
    default Object getField(){
        throw new WrongTypeException(this.getClass(),"getField()");
    }


    /**
     * @return Is the native value an arbitrary object
     */
    default boolean isCustom(){return false;}


    /**
     * @return Is the native value a string
     */
    default boolean isString(){return false;}


    /**
     * @return Is the native value a number (int, long, float, double)
     */
    default boolean isNumber(){return false;}


    /**
     * @return Is the native value a TsonMap
     */
    default boolean isMap(){return false;}


    /**
     * @return Is the native value a TsonList
     */
    default boolean isList(){return false;}


    /**
     * @return Is the native value a boolean
     */
    default boolean isBool(){return false;}


    /**
     * Converts an object to a Json (object) string
     * @return Json (object) string
     */
    default String toJsonObj(){return toString();}


    /**
     * Converts an object to a Json (dict) string
     * @return Json (dict) string
     */
    default String toJsonStr(){return toJsonObj();}


    /**
     * Writes the value represented as Tson to the builder.
     * Used for fast conversion to a Tson string
     */
    default void code(StringBuilder sb){
        sb.append(this);
    }


    /**
     * Writes the value represented as Json (object) to the builder.
     * Used for fast conversion to a Json (object) string
     */
    default void codeJsonObj(StringBuilder sb){
        code(sb);
    }


    /**
     * Writes the value represented as Json (dict) to the builder.
     * Used for fast conversion to a Json (dict) string
     */
    default void codeJson(StringBuilder sb){
        codeJsonObj(sb);
    }


    default TsonObj clone(){
        return this;
    }
}