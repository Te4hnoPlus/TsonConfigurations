package plus.tson.security;

import plus.tson.*;
import plus.tson.exception.NoSearchException;
import java.lang.reflect.InvocationTargetException;


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


    default Class<?> notFounded(String clazz){
        throw new NoSearchException(clazz+" not class");
    }
}