package plus.tson.utl;

import plus.tson.TsonFunc;
import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class FuncCompiler {
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static <R> Func0A<R> compile(Class<?> clazz, String method) {
        return (Func0A<R>) compileRaw(Func0A.class, clazz, method);
    }


    public static <R> Func1A<R> compile(Class<?> clazz, String method, Class<?> arg) {
        return (Func1A<R>) compileRaw(Func1A.class, clazz, method, arg);
    }


    public static <R> Func2A<R> compile(Class<?> clazz, String method, Class<?> arg1, Class<?> arg2) {
        return (Func2A<R>) compileRaw(Func2A.class, clazz, method, arg1, arg2);
    }


    public static <R> Func3A<R> compile(Class<?> clazz, String method, Class<?> arg1, Class<?> arg2, Class<?> arg3) {
        return (Func3A<R>) compileRaw(Func3A.class, clazz, method, arg1, arg2, arg3);
    }


    public static <R> Func4A<R> compile(Class<?> clazz, String method, Class<?> arg1, Class<?> arg2, Class<?> arg3, Class<?> arg4) {
        return (Func4A<R>) compileRaw(Func4A.class, clazz, method, arg1, arg2, arg3, arg4);
    }


    public static <R> Func5A<R> compile(Class<?> clazz, String method, Class<?> arg1, Class<?> arg2, Class<?> arg3, Class<?> arg4, Class<?> arg5) {
        return (Func5A<R>) compileRaw(Func5A.class, clazz, method, arg1, arg2, arg3, arg4, arg5);
    }


    public static <R> Func6A<R> compile(Class<?> clazz, String method, Class<?> arg1, Class<?> arg2, Class<?> arg3, Class<?> arg4, Class<?> arg5, Class<?> arg6) {
        return (Func6A<R>) compileRaw(Func6A.class, clazz, method, arg1, arg2, arg3, arg4, arg5, arg6);
    }


    public static Object compileRaw(Class<?> src, Class<?> clazz, String method, Class<?>... args) {
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
        return compile(mtd, srcOfCount(mtd.getParameterCount()));
    }


    public static Object compile(Method mtd, Class<?> src) {
        try {
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


    public static Func0A<?> compile(Field field){
        try {
            MethodHandle target = LOOKUP.unreflectGetter(field);

            MethodType func = target.type();
            CallSite site = LambdaMetafactory.metafactory(LOOKUP,
                    "call",
                    MethodType.methodType(Func0A.class),
                    func.erase(), target, func
            );

            Object result = site.getTarget().invoke();
            return (Func0A<?>) result;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public interface FuncS0A<R>{
        R call();
        default int args(){return 0;}
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


    public static TsonFunc makeFunc(Object inst, Object func){
        int args = countArgs(func);
        switch (args){
            case 0: return agrs -> ((Func0A)func).call(inst);
            case 1: return agrs -> ((Func1A)func).call(inst, agrs[0]);
            case 2: return agrs -> ((Func2A)func).call(inst, agrs[0], agrs[1]);
            case 3: return agrs -> ((Func3A)func).call(inst, agrs[0], agrs[1], agrs[2]);
            case 4: return agrs -> ((Func4A)func).call(inst, agrs[0], agrs[1], agrs[2], agrs[3]);
            case 5: return agrs -> ((Func5A)func).call(inst, agrs[0], agrs[1], agrs[2], agrs[3], agrs[4]);
            case 6: return agrs -> ((Func6A)func).call(inst, agrs[0], agrs[1], agrs[2], agrs[3], agrs[4], agrs[5]);
            default: throw new IllegalArgumentException("Too many arguments");
        }
    }


    public static class Compiler extends TsonFunc.Reflector{
        @Override
        public TsonFunc compile(Class<?> clazz, String name) {
            Method mtd = null;
            for (Method method:clazz.getDeclaredMethods()){
                if(method.getName().equals(name)){
                    if(mtd != null)
                        return super.compile(clazz, name);
                    mtd = method;
                }
            }
            if(mtd != null){
                return makeFunc(null, FuncCompiler.compile(mtd));
            }
            return super.compile(clazz, name);
        }


        @Override
        public TsonFunc compile(Object inst, String name) {
            Class<?> clazz = inst.getClass();
            Method mtd = null;
            for (Method method:clazz.getMethods()){
                if(method.getName().equals(name)){
                    if(mtd != null)
                        return super.compile(clazz, name);
                    mtd = method;
                }
            }
            if(mtd != null){
                Object func = FuncCompiler.compile(mtd);
                return makeFunc(inst, func);
            }
            return super.compile(clazz, name);
        }


        @Override
        public TsonFunc compile(TsonFunc.Frame frame) {
            return super.compile(frame);
        }
    }
}