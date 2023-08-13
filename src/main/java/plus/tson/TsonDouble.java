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


    @Override
    public TsonDouble clone() {
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (TsonDouble.class != o.getClass()) return false;
        return Double.compare(((TsonDouble) o).value, value) == 0;
    }


    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(value);
        return (int) (temp ^ (temp >>> 32));
    }
}