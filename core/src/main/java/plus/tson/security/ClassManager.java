package plus.tson.security;

import plus.tson.TsonClass;
import plus.tson.exception.NoSearchException;
import java.lang.reflect.InvocationTargetException;


public interface ClassManager {
    final class Def implements ClassManager{}

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