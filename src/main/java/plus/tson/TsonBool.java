package plus.tson;


public class TsonBool extends TsonPrimitive {
    private final boolean value;


    public TsonBool(boolean value) {
        this.value = value;
    }


    @Override
    public String getStr() {
        return value?"true":"false";
    }


    @Override
    public boolean getBool(){
        return value;
    }


    @Override
    public Boolean getField(){
        return value;
    }


    @Override
    public int getInt() {
        return value ? 1 : 0;
    }


    @Override
    public boolean isBool() {
        return true;
    }
}