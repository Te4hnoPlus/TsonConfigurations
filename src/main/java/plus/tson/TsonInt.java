package plus.tson;


public class TsonInt extends TsonPrimitive{
    private final int value;


    public TsonInt(int value) {
        this.value = value;
    }


    @Override
    public String getStr() {
        return Integer.toString(value);
    }


    @Override
    public int getInt() {
        return value;
    }


    @Override
    public Integer getField(){
        return value;
    }


    @Override
    public double getDouble() {
        return value;
    }


    @Override
    public float getFloat() {
        return value;
    }


    @Override
    public boolean isNumber(){
        return true;
    }
}
