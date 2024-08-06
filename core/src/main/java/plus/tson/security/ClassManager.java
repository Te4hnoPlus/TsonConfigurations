package plus.tson.security;

import plus.tson.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Manager to control creating custom objects and invoke methods
 */
public interface ClassManager {

    /**
     * Default class manager
     */
    final class Def implements ClassManager{}


    /**
     * Don`t allow creation any objects and invoke unsafe methods
     */
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


        /**
         * @throws SecurityException if try call static method
         */
        @Override
        public Object invoke(Class<?> clazz, Object inst, String methodName, AtomicBoolean nonVoid, Object... args) throws Exception {
            if(inst != null)throw new SecurityException("Can't invoke static method");
            return ClassManager.super.invoke(clazz, inst, methodName, nonVoid, args);
        }


        @Override
        public Class<?> forName(String clazz){
            return Object.class;
        }
    }


    /**
     * Constant prefix for class name
     */
    default String getAllowedPath(){
        return "";
    }


    /**
     * @return Class by name
     * @throws IllegalArgumentException if class not found
     */
    default Class<?> forName(String clazz){
        try {
            return Class.forName(getAllowedPath()+clazz);
        } catch (ClassNotFoundException e) {
            return notFounded(clazz);
        }
    }


    /**
     * Create object by class name and constructor args
     */
    default Object newInstance(String className, Object... args) throws Exception {
        return newInstance(forName(className), args);
    }


    /**
     * Create object by class and constructor args
     */
    default Object newInstance(Class<?> clazz, Object... args) throws Exception {
        return TsonClass.createInst(clazz, args);
    }


    /**
     * Call object`s method with args
     */
    default Object invoke(Object inst, String methodName, Object... args) throws Exception{
        return invoke(inst.getClass(), inst, methodName, null, args);
    }


    /**
     * Call object`s method with args
     * if nonVoid != null - assign it to function result == void.class
     */
    default Object invoke(Class<?> clazz, Object inst, String methodName, AtomicBoolean nonVoid, Object... args) throws Exception{
        return TsonClass.invoke(clazz, inst, methodName, nonVoid, args);
    }


    /**
     * Call object`s method with args
     */
    default Object invoke(Class<?> clazz, Object inst, String methodName, Object... args) throws Exception{
        return invoke(clazz, inst, methodName, null, args);
    }


    /**
     * Call object`s method with args
     * if nonVoid != null - assign it to function result == void.class
     */
    default Object invoke(Object inst, String methodName, AtomicBoolean nonVoid, Object... args) throws Exception{
        return invoke(inst.getClass(), inst, methodName, nonVoid, args);
    }


    /**
     * Called when class not founded
     */
    default Class<?> notFounded(String clazz){
        throw new IllegalArgumentException(clazz+" not class");
    }
}