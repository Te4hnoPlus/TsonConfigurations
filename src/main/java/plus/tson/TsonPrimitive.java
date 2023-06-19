package plus.tson;

import plus.tson.security.ClassManager;


public abstract class TsonPrimitive implements TsonObj{
    public static TsonPrimitive build(String value) {
        return build(new ClassManager.Def(), value);
    }


    public static TsonPrimitive build(ClassManager manager, String value) {
        value = value.trim();
        if(value.startsWith("(") && value.endsWith(")")){
            value = value.substring(1, value.length()-1);
        }
        if(value.equalsIgnoreCase("true")){
            return new TsonBool(true);
        } else if(value.equalsIgnoreCase("false")){
            return new TsonBool(false);
        }
        try {
            if (value.contains(".")) {
                if(value.length()>7){
                    return new TsonDouble(Double.parseDouble(value));
                } else {
                    return new TsonFloat(Float.parseFloat(value));
                }
            } else return new TsonInt(Integer.parseInt(value));
        } catch (NumberFormatException e){
            return new TsonClass(manager, value);
        }
    }


    @Override
    public String toString() {
        return '(' + getStr() + ')';
    }


    @Override
    public String toJsonStr() {
        return getStr();
    }
}