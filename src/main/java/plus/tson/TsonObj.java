package plus.tson;

import plus.tson.exception.WrongTypeException;

import java.util.List;


public interface TsonObj {

    default String getStr() {
        throw new WrongTypeException();
    }


    default int getInt() {
        throw new WrongTypeException();
    }


    default double getDouble() {
        throw new WrongTypeException();
    }


    default float getFloat(){
        throw new WrongTypeException();
    }


    default boolean getBool(){
        throw new WrongTypeException();
    }


    default TsonMap getMap() {
        throw new WrongTypeException();
    }


    default TsonList getList() {
        throw new WrongTypeException();
    }


    default Object getField(){
        throw new WrongTypeException();
    }


    default boolean isCustom(){return false;}


    default boolean isString(){return false;}


    default boolean isNumber(){return false;}


    default boolean isMap(){return false;}


    default boolean isList(){return false;}


    default boolean isBool(){return false;}
}