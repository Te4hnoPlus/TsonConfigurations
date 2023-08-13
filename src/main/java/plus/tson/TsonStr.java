package plus.tson;


public final class TsonStr implements TsonObj {
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
    public String toString() {
        return '"' + value + '"';
    }


    @Override
    public void code(StringBuilder sb) {
        sb.append('"').append(value).append('"');
    }


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
}