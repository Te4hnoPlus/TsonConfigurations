package plus.tson;

import plus.tson.security.ClassManager;
import plus.tson.utl.TsonMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;


/**
 * Tson proxy for any types
 */
public final class TsonField<T> implements TsonObj{
    public static final TsonField<?> NULL = new TsonField<>(null);
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


    public Object invoke(String methodName, Object... args){
        return invoke(methodName, null, args);
    }


    public Object invoke(String methodName, AtomicBoolean nonVoid, Object... args){
        try {
            return TsonClass.invoke(field.getClass(), field, methodName, nonVoid, args);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public TsonMethod method(String name, Class<?> ... args){
        return new TsonMethod(field.getClass(), name, args);
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


    @Override
    public void codeJsonObj(StringBuilder sb) {
        if(field instanceof TsonSerelizable)
            ((TsonSerelizable) field).toTson().codeJsonObj(sb);
        else
            throw new RuntimeException("Field ["+field.getClass()+"] ("+field+") cant be encode to json");
    }


    @Override
    public void codeJson(StringBuilder sb) {
        if(field instanceof TsonSerelizable)
            ((TsonSerelizable) field).toTson().codeJson(sb);
        else
            throw new RuntimeException("Field ["+field.getClass()+"] ("+field+") cant be encode to json");
    }


    private T cloneField() {
        try {
            Method mtd = field.getClass().getDeclaredMethod("clone");
            mtd.setAccessible(true);
            return (T) mtd.invoke(field);
        } catch (Exception e){
            e.printStackTrace();
            return field;
        }
    }


    @Override
    public TsonField<T> clone() {
        return new TsonField<>(cloneField());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;
        return field.equals(((TsonField<?>) o).field);
    }


    @Override
    public int hashCode() {
        return field.hashCode();
    }


    @Override
    public Type type() {
        return Type.CUSTOM;
    }
}