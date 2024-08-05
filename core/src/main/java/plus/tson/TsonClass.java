package plus.tson;

import plus.tson.exception.NoSearchException;
import plus.tson.security.ClassManager;
import plus.tson.utl.TsonMethod;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Tson proxy for the class type
 */
public final class TsonClass extends TsonPrimitive {
    private static final IdentityHashMap<Class<?>, Class<?>> eqMap;
    private static final Class<?>[] EMPTY = new Class[0];
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


    public Object createInst(ClassManager manager, Object... args){
        try {
            return manager.newInstance(clazz, args);
        } catch (Exception e) {
            return onError(e, args);
        }
    }


    public Object createInst(Object... args){
        try {
            return createInst(clazz, args);
        } catch (NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException e) {
            return onError(e, args);
        }
    }


    private Object onError(Exception e, Object... args){
        String[] strings = new String[args.length];
        for(int i = 0;i < strings.length; i++){
            strings[i] = args[i].getClass().getName();
        }
        e.printStackTrace();
        throw new NoSearchException(
                String.format("constructor not exist! %s(%s)",
                        clazz.getName(), String.join(", ",strings))
        );
    }


    public static Object createInstOf(String clazz, Object... args)
            throws ClassNotFoundException, InvocationTargetException,
            NoSuchMethodException, InstantiationException, IllegalAccessException {
        return createInst(Class.forName(clazz), args);
    }


    public static Object createInst(Class<?> clazz, Object... args)
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {

        Constructor<?> cons;
        if(args != null) {
            cons = tryFindConstructor(clazz, args);
        } else {
            cons = clazz.getDeclaredConstructor();
        }
        return createInst(cons, args);
    }


    /**
     * Use `soft` constructor`s selection
     */
    private static Constructor<?> tryFindConstructor(Class<?> clazz, Object... args){
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Class<?>[] classes = types(args);

        l1: for (Constructor<?> cns:constructors){
            if(cns.getParameterCount() == args.length){
                Class<?>[] params = cns.getParameterTypes();
                for (int i = 0; i < params.length; i++){
                    if(!isEqual(params[i], classes[i]))
                        continue l1;
                }
                return cns;
            }
        }
        return null;
    }


    private static boolean isEqual(Class<?> c1, Class<?> c2){
        if(c1 == c2)return true;
        if(eqMap.getOrDefault(c1, c1) == c2)return true;
        return c1.isAssignableFrom(c2);
    }


    public static Object createInst(Constructor<?> cons, Object... args)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        cons.setAccessible(true);
        return cons.newInstance(args);
    }


    public static Object invoke(Class<?> clazz, Object inst, String name, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return invoke(clazz, inst, name, null, args);
    }


    public static Object invoke(Class<?> clazz, Object inst, String name, AtomicBoolean nonVoid, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = clazz.getDeclaredMethod(name, types(args));
        if(nonVoid != null){
            nonVoid.set(method.getReturnType() == void.class);
        }
        return method.invoke(inst, args);
    }


    public TsonMethod method(String name, Class<?> ... args){
        return new TsonMethod(clazz, name, args);
    }


    public static Class<?>[] types(Object[] args){
        if(args == null || args.length == 0)return EMPTY;
        Class<?>[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            classes[i] = args[i].getClass();
        }
        return classes;
    }


    /**
     * Due to the fact that the nested value is not modifiable, cloning is ignored for optimization purposes.
     */
    @Override
    public TsonClass clone() {
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (TsonClass.class != o.getClass()) return false;
        return clazz == ((TsonClass) o).clazz;
    }


    @Override
    public int hashCode() {
        return clazz != null ? clazz.hashCode() : 0;
    }


    @Override
    public String getStr() {
        return clazz.toString();
    }


    @Override
    public Type type() {
        return Type.CLASS;
    }


    static {
        IdentityHashMap<Class<?>, Class<?>> map = eqMap = new IdentityHashMap<>();

        map.put(boolean.class, Boolean.class);
        map.put(Boolean.class, boolean.class);
        map.put(byte.class, Byte.class);
        map.put(Byte.class, byte.class);
        map.put(char.class, Character.class);
        map.put(Character.class, char.class);
        map.put(double.class, Double.class);
        map.put(Double.class, double.class);
        map.put(float.class, Float.class);
        map.put(Float.class, float.class);
        map.put(int.class, Integer.class);
        map.put(Integer.class, int.class);
        map.put(long.class, Long.class);
        map.put(Long.class, long.class);
        map.put(short.class, Short.class);
        map.put(Short.class, short.class);
    }
}