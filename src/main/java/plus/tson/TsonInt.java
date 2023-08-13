package plus.tson;


public final class TsonInt extends TsonPrimitive{
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


    @Override
    public void code(StringBuilder sb) {
        sb.append('(').append(value).append(')');
    }


    @Override
    public TsonInt clone() {
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;
        TsonInt tsonInt = (TsonInt) o;
        return value == tsonInt.value;
    }


    @Override
    public int hashCode() {
        return value;
    }
}
