package plus.tson;

import plus.tson.exception.WrongTypeException;


public interface TsonObj extends Cloneable{

    default String getStr() {
        throw new WrongTypeException(this.getClass(),"getStr()");
    }


    default int getInt() {
        throw new WrongTypeException(this.getClass(),"getInt()");
    }


    default double getDouble() {
        throw new WrongTypeException(this.getClass(),"getDouble()");
    }


    default long getLong(){
        throw new WrongTypeException(this.getClass(),"getLong()");
    }


    default float getFloat(){
        throw new WrongTypeException(this.getClass(),"getFloat()");
    }


    default boolean getBool(){
        throw new WrongTypeException(this.getClass(),"getBool()");
    }


    default TsonMap getMap() {
        throw new WrongTypeException(this.getClass(),"getMap()");
    }


    default TsonList getList() {
        throw new WrongTypeException(this.getClass(),"getList()");
    }


    default Object getField(){
        throw new WrongTypeException(this.getClass(),"getField()");
    }


    default boolean isCustom(){return false;}


    default boolean isString(){return false;}


    default boolean isNumber(){return false;}


    default boolean isMap(){return false;}


    default boolean isList(){return false;}


    default boolean isBool(){return false;}


    default String toJsonObj(){return toString();}


    default String toJsonStr(){return toJsonObj();}


    default void code(StringBuilder sb){
        sb.append(this);
    }


    default void codeJsonObj(StringBuilder sb){
        code(sb);
    }


    default void codeJson(StringBuilder sb){
        codeJsonObj(sb);
    }


    default TsonObj clone(){
        return this;
    }
}