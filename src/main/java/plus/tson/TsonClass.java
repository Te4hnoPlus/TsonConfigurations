package plus.tson;

import plus.tson.exception.NoSearchException;
import plus.tson.security.ClassManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public final class TsonClass extends TsonPrimitive {
    private final Class<?> clazz;

    public TsonClass(String clazz){
        this(new ClassManager.Def(), clazz);
    }


    public TsonClass(ClassManager manager, String clazz){
        this.clazz = manager.forName(clazz);
    }


    public TsonClass(Class<?> clazz) {
        this.clazz = clazz;
    }


    @Override
    public Class<?> getField(){
        return clazz;
    }


    public Object createInstBy(String constructor, Object... args){
        try {
            String[] items = constructor.replace("\n","").split(",");
            Class<?>[] classes = new Class[items.length];
            for (int i = 0; i < classes.length; i++) {
                classes[i] = Class.forName(items[i].trim());
            }
            Constructor<?> cons = clazz.getDeclaredConstructor(classes);
            return createInst(cons, args);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    public Object createInst(Object... args){
        try {
            return createInst(clazz, args);
        } catch (NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException e) {
            String[] strings = new String[args.length];
            for(int i=0;i<strings.length;i++){
                strings[i] = args[i].getClass().getName();
            }
            e.printStackTrace();
            throw new NoSearchException(
                    String.format("constructor not exist! %s(%s)",
                            clazz.getName(), String.join(", ",strings))
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
                cons = clazz.getDeclaredConstructor();
                break;
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
        return createInst(cons, args);
    }


    public static Object createInst(Constructor<?> cons, Object... args)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        cons.setAccessible(true);
        return cons.newInstance(args);
    }
}