package plus.tson;


public class TsonField<T> implements TsonObj{
    private final T field;

    public static TsonField<?> build(String value){
        value = value.trim();
        if(value.startsWith("<") && value.endsWith(">")){
            value = value.substring(1, value.length()-1);
        }
        return new TsonField<>(TsonMap.gen(value));
    }


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
        if(field instanceof TsonSerelizable) {
            return "<(" + field.getClass().getName() + "), " +
                    ((TsonSerelizable) field).toTson().toString() + '>';
        } else {
            return "<(" + field.getClass().getName() +">) TSON NOT SUPPORTED>";
        }
    }
}