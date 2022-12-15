package plus.tson;

import plus.tson.exception.NoSearchException;
import java.lang.reflect.Field;
import java.util.List;
import static plus.tson.TsonMap.*;


public class TsonField<T> implements TsonObj{
    private final T field;

    public static TsonField<?> build(String data){
        data = data.trim();
        if(data.startsWith("<") && data.endsWith(">")){
            data = data.substring(1, data.length()-1).trim();
        }
        if(data.isEmpty()) return null;

        if(data.charAt(0)=='&'){
            return new TsonField<>(getExistObj(data));
        }
        return new TsonField<>(genObj(data));
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


    private static Object getExistObj(String data){
        List<String> values = split(data,startSeparators, endSeparators, objectSeparator);

        if(values.size()>2)throw new NoSearchException("generator syntax: <(CLASS), {data=\"example\"}>");

        TsonClass cl = new TsonClass(getSubData(values.get(0), '(', ')'));

        Field f = null;
        String field = getSubData(values.get(1), '"');
        try {
            f = cl.getField().getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            throw new NoSearchException("Class ("+cl.getField().getName()+") hasn't static Object ("+field+")!");
        }
        f.setAccessible(true);

        try {
            return f.get(null);
        } catch (IllegalAccessException e) {
           return null;
        }
    }


    private static Object genObj(String data){
        List<String> values = split(data,startSeparators, endSeparators, objectSeparator);

        if(values.size()>7)throw new NoSearchException("TsonField support no more than 6 arguments except for the class!");

        TsonClass cl = new TsonClass(getSubData(values.get(0), '(', ')'));

        if(values.size()==1){
            return cl.createInst();
        }

        Object[] objects = new Object[Math.min(values.size()-1, 6)];

        for(int i=1;i< values.size() && i<7;i++){
            String raw = values.get(i).trim();
            switch (TsonObjType.scanType(raw.charAt(0))){
                case STR:
                    objects[i-1] = getSubData(raw, '"');
                    break;
                case LIST:
                    objects[i-1] = new TsonList(raw);
                    break;
                case BASIC:
                    objects[i-1] = TsonPrimitive.build(raw).getField();
                    break;
                case MAP:
                    objects[i-1] = new TsonMap(raw);
                    break;
                case FIELD:
                    objects[i-1] = TsonField.build(raw).getField();
                    break;
            }
        }
        return cl.createInst(objects);
    }
}