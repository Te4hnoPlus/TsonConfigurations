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
    public String toString() {
        return '"' + value + '"';
    }


    @Override
    public void code(StringBuilder sb) {
        sb.append('"').append(value).append('"');
    }
}