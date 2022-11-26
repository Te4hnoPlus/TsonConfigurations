package plus.tson;


public class TsonField<T extends TsonSerelizable> implements TsonObj{
    private final T field;

    public TsonField(T field) {
        this.field = field;
    }


    @Override
    public T getField(){
        return field;
    }


    @Override
    public boolean isCustom(){
        return true;
    }


    @Override
    public String getStr(){
        return field.toString();
    }


    @Override
    public String toString() {
        return "<(" + field.getClass().getName() + "), " + field.toMap().toString() + '>';
    }
}