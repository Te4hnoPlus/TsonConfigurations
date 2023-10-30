package plus.tson.utl;

import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;


public class JSceme<T> {
    private final Te4HashMap<String,Field> fields = new Te4HashMap<>();
    private final Te4HashMap<String,JSceme> sub = new Te4HashMap<>();
    private final Constructor<T> constructor;

    public JSceme(Class<T> clazz){
        for (Field f:clazz.getDeclaredFields()){
            Class<?> clazz0 = f.getType();
            if(clazz0 == int.class || clazz0 == float.class || clazz0 == double.class || clazz0 == String.class ||
                    clazz0 == TsonList.class || clazz0 == TsonObj.class || clazz0 == TsonMap.class
            ){
                fields.fput(f.getName(), f);
            } else {
                sub.fput(f.getName(), new JSceme<>(clazz0));
            }
        }
        try {
            constructor = clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    public T parse(String s){
        return new JScemParser(s.getBytes()).result(this);
    }


    public T newObj(){
        try {
            return constructor.newInstance();
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public void set(String key, T obj, int i){
        Field field = fields.get(key);
        if(field==null)return;
        try {
            field.setInt(obj, i);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public void set(String key, T obj, float f){
        Field field = fields.get(key);
        if(field==null)return;
        try {
            field.setFloat(obj, f);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public JSceme sub(String key){
        return sub.get(key);
    }


    public void set(String key, T obj, double d){
        Field field = fields.get(key);
        if(field==null)return;
        try {
            field.setDouble(obj, d);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public void set(String key, T obj, boolean b){
        Field field = fields.get(key);
        if(field==null)return;
        try {
            field.setBoolean(obj, b);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public void set(String key, T obj, Object o){
        Field field = fields.get(key);
        if(field==null)return;
        try {
            field.set(obj, o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}