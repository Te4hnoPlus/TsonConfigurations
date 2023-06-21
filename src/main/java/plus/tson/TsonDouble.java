package plus.tson;


public final class TsonDouble extends TsonPrimitive{
    private final double value;


    public TsonDouble(double value) {
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
    public Double getField(){
        return value;
    }


    @Override
    public double getDouble() {
        return value;
    }


    @Override
    public float getFloat() {
        return (float) value;
    }


    @Override
    public boolean isNumber(){
        return true;
    }


    @Override
    public void code(StringBuilder sb) {
        sb.append('(').append(value).append(')');
    }
}