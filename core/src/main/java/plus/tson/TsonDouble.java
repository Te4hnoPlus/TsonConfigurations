package plus.tson;


/**
 * Tson proxy for the double type
 */
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
    public long getLong() {
        return (long) value;
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
    public void codeJsonObj(StringBuilder sb) {
        sb.append(value);
    }


    /**
     * Due to the fact that the nested value is not modifiable, cloning is ignored for optimization purposes.
     */
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


    @Override
    public Type type() {
        return Type.DOUBLE;
    }
}