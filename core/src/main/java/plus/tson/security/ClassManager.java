package plus.tson.security;

import plus.tson.*;
import plus.tson.exception.NoSearchException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;


public interface ClassManager {
    final class Def implements ClassManager{}
    final class Empty implements ClassManager{
        @Override
        public Object newInstance(String className, Object... args) throws Exception {
            TsonList list = new TsonList(args.length+1);
            list.add(className);
            for (Object arg : args) {
                if (arg instanceof TsonObj) {
                    list.add((TsonObj) arg);
                } else {
                    list.add(new TsonField<>(arg));
                }
            }
            return list;
        }
        @Override
        public Class<?> forName(String clazz){
            return Object.class;
        }
    }


    default String getAllowedPath(){
        return "";
    }


    default Class<?> forName(String clazz){
        try {
            return Class.forName(getAllowedPath()+clazz);
        } catch (ClassNotFoundException e) {
            return notFounded(clazz);
        }
    }


    default Object newInstance(String className, Object... args) throws Exception {
        return newInstance(forName(className), args);
    }


    default Object newInstance(Class<?> clazz, Object... args) throws Exception {
        return TsonClass.createInst(clazz, args);
    }


    default Object invoke(Object inst, String methodName, Object... args) throws Exception{
        return invoke(inst.getClass(), inst, methodName, null, args);
    }


    default Object invoke(Class<?> clazz, Object inst, String methodName, AtomicBoolean nonVoid, Object... args) throws Exception{
        return TsonClass.invoke(clazz, inst, methodName, nonVoid, args);
    }


    default Object invoke(Class<?> clazz, Object inst, String methodName, Object... args) throws Exception{
        return invoke(clazz, inst, methodName, null, args);
    }


    default Object invoke(Object inst, String methodName, AtomicBoolean nonVoid, Object... args) throws Exception{
        return invoke(inst.getClass(), inst, methodName, nonVoid, args);
    }


    default Class<?> notFounded(String clazz){
        throw new NoSearchException(clazz+" not class");
    }
}