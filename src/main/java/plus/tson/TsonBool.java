package plus.tson;


public final class TsonBool extends TsonPrimitive {
    public static final TsonBool TRUE = new TsonBool(true);
    public static final TsonBool FALSE = new TsonBool(false);
    private final boolean value;


    public TsonBool(boolean value) {
        this.value = value;
    }


    @Override
    public String getStr() {
        return value?"true":"false";
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
    public boolean isBool() {
        return true;
    }


    @Override
    public void code(StringBuilder sb) {
        sb.append('(').append(value).append(')');
    }


    @Override
    public TsonBool clone() {
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (TsonBool.class != o.getClass()) return false;
        TsonBool tsonBool = (TsonBool) o;
        return value == tsonBool.value;
    }


    @Override
    public int hashCode() {
        return (value ? 1 : 0);
    }
}