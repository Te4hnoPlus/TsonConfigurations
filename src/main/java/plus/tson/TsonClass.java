package plus.tson;

import plus.tson.exception.NoSearchException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;


public class TsonClass extends TsonPrimitive {
    private final Class<?> clazz;

    public TsonClass(String clazz){
        try {
            this.clazz = Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            throw new NoSearchException(clazz+" not class");
        }
    }


    public TsonClass(Class<?> clazz) {
        this.clazz = clazz;
    }


    @Override
    public Class<?> getField(){
        return clazz;
    }


    public Object createInst(Object... args){
        try {
            return createInst(clazz, args);
        } catch (NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException e) {
            String[] strings = new String[args.length];
            Arrays.fill(strings, args.getClass().getName());
            throw new NoSearchException(
                    String.format("constructor not exist for (%s)", String.join(", ",strings))
            );
        }
    }


    public static Object createInst(String clazz, Object... args)
            throws ClassNotFoundException, InvocationTargetException,
            NoSuchMethodException, InstantiationException, IllegalAccessException {
        return createInst(Class.forName(clazz), args);
    }


    public static Object createInst(Class<?> clazz, Object... args)
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {

        Constructor<?> cons;
        switch (args.length){
            default:
                return clazz.newInstance();
            case 1:
                cons = clazz.getDeclaredConstructor(args[0].getClass());
                break;
            case 2:
                cons = clazz.getDeclaredConstructor(
                        args[0].getClass(), args[1].getClass()
                );
                break;
            case 3:
                cons = clazz.getDeclaredConstructor(
                        args[0].getClass(), args[1].getClass(),
                        args[2].getClass()
                );
                break;
            case 4:
                cons = clazz.getDeclaredConstructor(
                        args[0].getClass(), args[1].getClass(),
                        args[2].getClass(), args[3].getClass()
                );
                break;
            case 5:
                cons = clazz.getDeclaredConstructor(
                        args[0].getClass(), args[1].getClass(),args[2].getClass(),
                        args[3].getClass(), args[4].getClass()
                );
                break;
            case 6:
                cons = clazz.getDeclaredConstructor(
                        args[0].getClass(), args[1].getClass(),args[2].getClass(),
                        args[3].getClass(), args[4].getClass(),args[5].getClass()
                );
                break;
        }
        return cons.newInstance(args);
    }
}