package plus.tson;


/**
 * Tson proxy for the float type
 */
public final class TsonFloat extends TsonPrimitive{
    private final float value;

    public TsonFloat(float value) {
        this.value = value;
    }


    @Override
    public String getStr() {
        return Float.toString(value);
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
    public long getLong() {
        return (long) value;
    }


    @Override
    public double getDouble() {
        return value;
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
    public TsonFloat clone() {
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (TsonFloat.class != o.getClass()) return false;
        return Float.compare(((TsonFloat) o).value, value) == 0;
    }


    @Override
    public int hashCode() {
        return (value != +0.0f ? Float.floatToIntBits(value) : 0);
    }


    @Override
    public Type type() {
        return Type.FLOAT;
    }
}
