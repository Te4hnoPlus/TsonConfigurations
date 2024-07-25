package plus.tson.utl;

import java.lang.reflect.Method;


public class TsonMethod {
    private final Method method;
    private final boolean isVoid;

    public TsonMethod(Class<?> clazz, String name, Class<?>... args) {
        try {
            this.method = clazz.getDeclaredMethod(name, args);
            method.setAccessible(true);
            this.isVoid = method.getReturnType() == void.class;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    public Object invokeStatic(Object... args) {
        try {
            return method.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void invokeStaticV(Object... args) {
        try {
            method.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Object invoke(Object inst, Object... args) {
        try {
            return method.invoke(inst, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void invokeV(Object inst, Object... args) {
        try {
            method.invoke(inst, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public boolean isVoid() {
        return isVoid;
    }
}