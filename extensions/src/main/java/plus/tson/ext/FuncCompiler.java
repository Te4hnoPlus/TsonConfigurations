package plus.tson.ext;

import java.lang.invoke.*;
import java.lang.reflect.Method;


public class FuncCompiler {
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static <R> Func0A<R> compile(Class<?> clazz, String method) {
        return (Func0A<R>) compile0(Func0A.class, clazz, method);
    }


    public static <R> Func1A<R> compile(Class<?> clazz, String method, Class<?> arg) {
        return (Func1A<R>) compile0(Func1A.class, clazz, method, arg);
    }


    public static <R> Func2A<R> compile(Class<?> clazz, String method, Class<?> arg1, Class<?> arg2) {
        return (Func2A<R>) compile0(Func2A.class, clazz, method, arg1, arg2);
    }


    public static <R> Func3A<R> compile(Class<?> clazz, String method, Class<?> arg1, Class<?> arg2, Class<?> arg3) {
        return (Func3A<R>) compile0(Func3A.class, clazz, method, arg1, arg2, arg3);
    }


    public static <R> Func4A<R> compile(Class<?> clazz, String method, Class<?> arg1, Class<?> arg2, Class<?> arg3, Class<?> arg4) {
        return (Func4A<R>) compile0(Func4A.class, clazz, method, arg1, arg2, arg3, arg4);
    }


    public static <R> Func5A<R> compile(Class<?> clazz, String method, Class<?> arg1, Class<?> arg2, Class<?> arg3, Class<?> arg4, Class<?> arg5) {
        return (Func5A<R>) compile0(Func5A.class, clazz, method, arg1, arg2, arg3, arg4, arg5);
    }


    public static <R> Func6A<R> compile(Class<?> clazz, String method, Class<?> arg1, Class<?> arg2, Class<?> arg3, Class<?> arg4, Class<?> arg5, Class<?> arg6) {
        return (Func6A<R>) compile0(Func6A.class, clazz, method, arg1, arg2, arg3, arg4, arg5, arg6);
    }


    private static Object compile0(Class<?> src, Class<?> clazz, String method, Class<?>... args) {
        try {
            Method mtd = clazz.getMethod(method, args);
            MethodHandle target = LOOKUP.unreflect(mtd);

            MethodType func = target.type();
            CallSite site = LambdaMetafactory.metafactory(LOOKUP,
                    "call",
                    MethodType.methodType(src),
                    func.erase(), target, func
            );

            Object result = site.getTarget().invoke();
            return result;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public static Object compile(Method mtd) {
        try {
            Class<?> src = srcOfCount(mtd.getParameterCount());

            MethodHandle target = LOOKUP.unreflect(mtd);

            MethodType func = target.type();
            CallSite site = LambdaMetafactory.metafactory(LOOKUP,
                    "call",
                    MethodType.methodType(src),
                    func.erase(), target, func
            );

            Object result = site.getTarget().invoke();
            return result;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public interface Func0A<R>{
        R call(Object inst);
        default int args(){return 0;}
    }
    public interface Func1A<R>{
        R call(Object inst, Object arg);
        default int args(){return 1;}
    }
    public interface Func2A<R>{
        R call(Object inst, Object arg1, Object arg2);
        default int args(){return 2;}
    }
    public interface Func3A<R>{
        R call(Object inst, Object arg1, Object arg2, Object arg3);
        default int args(){return 3;}
    }
    public interface Func4A<R>{
        R call(Object inst, Object arg1, Object arg2, Object arg3, Object arg4);
        default int args(){return 4;}
    }
    public interface Func5A<R>{
        R call(Object inst, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5);
        default int args(){return 5;}
    }
    public interface Func6A<R>{
        R call(Object inst, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6);
        default int args(){return 6;}
    }


    public static int countArgs(Object func){
        for (Method mtd:func.getClass().getMethods()){
            if(mtd.getName().equals("call")){
                return mtd.getParameterCount();
            }
        }
        return -1;
    }


    public static <R> R invoke(Object func, Object inst, Object... args){
        switch (args.length){
            case 0: return ((Func0A<R>)func).call(inst);
            case 1: return ((Func1A<R>)func).call(inst, args[0]);
            case 2: return ((Func2A<R>)func).call(inst, args[0], args[1]);
            case 3: return ((Func3A<R>)func).call(inst, args[0], args[1], args[2]);
            case 4: return ((Func4A<R>)func).call(inst, args[0], args[1], args[2], args[3]);
            case 5: return ((Func5A<R>)func).call(inst, args[0], args[1], args[2], args[3], args[4]);
            case 6: return ((Func6A<R>)func).call(inst, args[0], args[1], args[2], args[3], args[4], args[5]);
            default: throw new IllegalArgumentException("Too many arguments");
        }
    }


    public static Object invokeRaw(Object func, Object inst, Object... args){
        switch (args.length){
            case 0: return ((Func0A)func).call(inst);
            case 1: return ((Func1A)func).call(inst, args[0]);
            case 2: return ((Func2A)func).call(inst, args[0], args[1]);
            case 3: return ((Func3A)func).call(inst, args[0], args[1], args[2]);
            case 4: return ((Func4A)func).call(inst, args[0], args[1], args[2], args[3]);
            case 5: return ((Func5A)func).call(inst, args[0], args[1], args[2], args[3], args[4]);
            case 6: return ((Func6A)func).call(inst, args[0], args[1], args[2], args[3], args[4], args[5]);
            default: throw new IllegalArgumentException("Too many arguments");
        }
    }


    public static Class<?> srcOfCount(int count){
        switch (count){
            case 0:return Func0A.class;
            case 1:return Func1A.class;
            case 2:return Func2A.class;
            case 3:return Func3A.class;
            case 4:return Func4A.class;
            case 5:return Func5A.class;
            case 6:return Func6A.class;
            default: throw new IllegalArgumentException("Too many arguments");
        }
    }
}