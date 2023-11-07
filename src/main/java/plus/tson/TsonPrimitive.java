package plus.tson;

import plus.tson.security.ClassManager;


public abstract class TsonPrimitive implements TsonObj{
    TsonPrimitive(){}


    public static TsonPrimitive build(ClassManager manager, String value) {
        return new TsonParser(manager, value).goTo('(').getBasic();
    }


    public static TsonPrimitive build(String value) {
        return new TsonParser(value).goTo('(').getBasic();
    }


    @Override
    public String toString() {
        return '(' + getStr() + ')';
    }


    @Override
    public String toJsonObj() {
        return getStr();
    }


    @Override
    public TsonPrimitive clone() {
        return this;
    }
}