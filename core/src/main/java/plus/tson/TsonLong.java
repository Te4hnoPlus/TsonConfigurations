package plus.tson;


/**
 * Tson proxy for the long type
 */
public class TsonLong extends TsonPrimitive{
    private final long value;

    public TsonLong(long value) {
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
    public Long getField(){
        return value;
    }


    @Override
    public double getDouble() {
        return value;
    }


    @Override
    public long getLong() {
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
    public void codeJsonObj(StringBuilder sb) {
        sb.append(value);
    }


    /**
     * Due to the fact that the nested value is not modifiable, cloning is ignored for optimization purposes.
     */
    @Override
    public TsonLong clone() {
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (TsonLong.class != o.getClass()) return false;
        return ((TsonLong) o).value == value;
    }


    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }


    @Override
    public Type type() {
        return Type.LONG;
    }
}
