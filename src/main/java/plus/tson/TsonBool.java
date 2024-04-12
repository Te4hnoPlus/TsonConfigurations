package plus.tson;


/**
 * Tson proxy for the boolean type
 * <br>
 * It is recommended not to create instances of this class manually
 */
public final class TsonBool extends TsonPrimitive {
    public static final TsonBool TRUE = new TsonBool(true);
    public static final TsonBool FALSE = new TsonBool(false);
    private final boolean value;

    public TsonBool(boolean value) {
        this.value = value;
    }


    @Override
    public String getStr() {
        return value ? "true" : "false";
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
    public double getDouble() {
        return value ? 1 : 0;
    }


    @Override
    public long getLong() {
        return value ? 1 : 0;
    }


    @Override
    public float getFloat() {
        return value ? 1 : 0;
    }


    @Override
    public boolean isBool() {
        return true;
    }


    @Override
    public void code(StringBuilder sb) {
        sb.append(value);
    }


    @Override
    public void codeJsonObj(StringBuilder sb) {
        sb.append(value);
    }


    /**
     * Due to the fact that the nested value is not modifiable, cloning is ignored for optimization purposes.
     */
    @Override
    public TsonBool clone() {
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (TsonBool.class != o.getClass()) return false;
        return value == ((TsonBool) o).value;
    }


    @Override
    public int hashCode() {
        return (value ? 1 : 0);
    }
}