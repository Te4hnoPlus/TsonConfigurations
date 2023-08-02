package plus.tson;

import plus.tson.security.ClassManager;


public final class TsonField<T> implements TsonObj{
    private final T field;

    public static TsonField<?> build(ClassManager manager, String data){
        return new TsonParser(manager, data).goTo('<').getField();
    }


    public static TsonField<?> build(String data){
        return new TsonParser(data).goTo('<').getField();
    }


    public TsonField(ClassManager manager, String data){
        field = (T) new TsonParser(manager, data).getFieldObj();
    }


    public TsonField(T field) {
        this.field = field;
    }


    @Override
    public T getField(){
        return field;
    }


    public <E> E getField(Class<E> type){
        return (E) field;
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
            return "<(" + field.getClass().getName() + ")," +
                    ((TsonSerelizable) field).toTson().toString() + '>';
        } else {
            return "<(" + field.getClass().getName() +">),\""+ field +"\">";
        }
    }


    @Override
    public void code(StringBuilder sb) {
        if(field instanceof TsonSerelizable) {
            sb.append("<(").append(field.getClass().getName()).append("),")
                    .append(((TsonSerelizable) field).toTson().toString()).append('>');
        } else {
            sb.append("<(").append(field.getClass().getName()).append("),\"")
                    .append(field.toString()).append("\">");
        }
    }
}