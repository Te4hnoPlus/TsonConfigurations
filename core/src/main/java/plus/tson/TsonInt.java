package plus.tson;


/**
 * Tson proxy for the int type
 */
public final class TsonInt extends TsonPrimitive{
    public static final TsonInt ZERO = new TsonInt(0);
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
    public long getLong() {
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
    public TsonInt clone() {
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (TsonInt.class != o.getClass()) return false;
        return value == ((TsonInt) o).value;
    }


    @Override
    public int hashCode() {
        return value;
    }


    @Override
    public Type type() {
        return Type.INT;
    }
}
