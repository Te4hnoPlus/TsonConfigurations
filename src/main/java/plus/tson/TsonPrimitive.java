package plus.tson;

public abstract class TsonPrimitive implements TsonObj{

    public static TsonPrimitive build(String value) {
        if(value.equalsIgnoreCase("true")){
            return new TsonBool(true);
        } else if(value.equalsIgnoreCase("false")){
            return new TsonBool(false);
        }
        try {
            if (value.contains(".")) {
                return new TsonDouble(Double.parseDouble(value.trim()));
            } else return new TsonInt(Integer.parseInt(value.trim()));
        } catch (NumberFormatException e){
            return new TsonClass(value);
        }
    }


    @Override
    public String toString() {
        return '(' + getStr() + ')';
    }
}