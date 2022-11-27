package plus.tson;

public class TsonFloat extends TsonPrimitive{
    private final float value;


    public TsonFloat(float value) {
        this.value = value;
    }


    @Override
    public String getStr() {
        return Double.toString(value);
    }


    @Override
    public int getInt() {
        return (int) value;
    }


    @Override
    public Float getField(){
        return value;
    }


    @Override
    public float getFloat() {
        return value;
    }


    @Override
    public double getDouble() {
        return value;
    }


    @Override
    public boolean isNumber(){
        return true;
    }
}
