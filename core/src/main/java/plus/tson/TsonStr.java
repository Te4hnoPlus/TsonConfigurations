package plus.tson;


/**
 * Tson proxy for the string type
 */
public final class TsonStr implements TsonObj, CharSequence {
    private final String value;

    public TsonStr(String value) {
        this.value = value;
    }


    @Override
    public String getStr() {
        return value;
    }


    @Override
    public boolean isString(){return true;}


    @Override
    public String getField(){
        return value;
    }


    @Override
    public int length() {
        return value.length();
    }


    @Override
    public char charAt(int index) {
        return value.charAt(index);
    }


    @Override
    public CharSequence subSequence(int start, int end) {
        return new TsonStr(value.subSequence(start, end).toString());
    }


    @Override
    public String toString() {
        return '"' + value + '"';
    }


    @Override
    public void code(StringBuilder sb) {
        sb.append('"').append(value).append('"');
    }


    /**
     * Due to the fact that the nested value is not modifiable, cloning is ignored for optimization purposes.
     */
    @Override
    public TsonStr clone() {
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (TsonStr.class != o.getClass()) return false;
        return value.equals(((TsonStr) o).value);
    }


    @Override
    public int hashCode() {
        return value.hashCode();
    }


    @Override
    public Type type() {
        return Type.STRING;
    }
}