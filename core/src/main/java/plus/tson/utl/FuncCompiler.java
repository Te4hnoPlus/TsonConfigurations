package plus.tson.utl;

import plus.tson.TsonFunc;
import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


/**
 * Utils to compile reflections to lambdas
 */
public class FuncCompiler {
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Compile lambda from method without args
     */
    public static <R> Func0A<R> compile(Class<?> clazz, String method) {
        return (Func0A<R>) compileRaw(Func0A.class, clazz, method);
    }


    /**
     * Compile lambda from method with one arg
     */
    public static <R> Func1A<R> compile(Class<?> clazz, String method, Class<?> arg) {
        return (Func1A<R>) compileRaw(Func1A.class, clazz, method, arg);
    }


    /**
     * Compile lambda from method with 2 args
     */
    public static <R> Func2A<R> compile(Class<?> clazz, String method, Class<?> arg1, Class<?> arg2) {
        return (Func2A<R>) compileRaw(Func2A.class, clazz, method, arg1, arg2);
    }


    /**
     * Compile lambda from method with 3 args
     */
    public static <R> Func3A<R> compile(Class<?> clazz, String method, Class<?> arg1, Class<?> arg2, Class<?> arg3) {
        return (Func3A<R>) compileRaw(Func3A.class, clazz, method, arg1, arg2, arg3);
    }


    /**
     * Compile lambda from method with 4 args
     */
    public static <R> Func4A<R> compile(Class<?> clazz, String method, Class<?> arg1, Class<?> arg2, Class<?> arg3, Class<?> arg4) {
        return (Func4A<R>) compileRaw(Func4A.class, clazz, method, arg1, arg2, arg3, arg4);
    }


    /**
     * Compile lambda from method with 5 args
     */
    public static <R> Func5A<R> compile(Class<?> clazz, String method, Class<?> arg1, Class<?> arg2, Class<?> arg3, Class<?> arg4, Class<?> arg5) {
        return (Func5A<R>) compileRaw(Func5A.class, clazz, method, arg1, arg2, arg3, arg4, arg5);
    }


    /**
     * Compile lambda from method with 6 args
     */
    public static <R> Func6A<R> compile(Class<?> clazz, String method, Class<?> arg1, Class<?> arg2, Class<?> arg3, Class<?> arg4, Class<?> arg5, Class<?> arg6) {
        return (Func6A<R>) compileRaw(Func6A.class, clazz, method, arg1, arg2, arg3, arg4, arg5, arg6);
    }


    /**
     * Compile lambda from method. Don`t support more than 6 args
     * @param src Interface to be used as a template
     * @param clazz Target class
     * @param method Method name
     * @param args Method args
     */
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


    /**
     * Compile reflect method to lambda, interface will be chosen automatically
     * @param mtd Target method
     */
    public static Object compile(Method mtd) {
        return compile(mtd, srcOfCount(mtd.getParameterCount()));
    }


    /**
     * Compile reflect method to lambda
     * @param mtd Target method
     * @param src Interface to be used as a template
     */
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


    /**
     * Compile field accessor to lambda
     */
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


    /**
     * Templates for lambda compilation (0 - 6 args)
     */
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


    /**
     * @param func Interface (Func0A, Func1A, ..., Func6A)
     * @return count of args
     */
    public static int countArgs(Object func){
        for (Method mtd:func.getClass().getMethods()){
            if(mtd.getName().equals("call")){
                return mtd.getParameterCount();
            }
        }
        return -1;
    }


    /**
     * Invoke FuncCompiler`s function and auto cast result
     * @param func Interface (Func0A, Func1A, ..., Func6A)
     * @param inst Target instance
     * @param args Arguments
     */
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


    /**
     * Invoke FuncCompiler`s function
     * @param func Interface (Func0A, Func1A, ..., Func6A)
     * @param inst Target instance
     * @param args Arguments
     */
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


    /**
     * @param count Choose interface template for number of arguments
     */
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


    /**
     * Wrap raw FuncCompiler`s function to TsonFunc
     */
    public static TsonFunc makeFunc(Object inst, Object func){
        int count = countArgs(func);
        switch (count){
            case 0: return args -> ((Func0A)func).call(inst);
            case 1: return args -> ((Func1A)func).call(inst, args[0]);
            case 2: return args -> ((Func2A)func).call(inst, args[0], args[1]);
            case 3: return args -> ((Func3A)func).call(inst, args[0], args[1], args[2]);
            case 4: return args -> ((Func4A)func).call(inst, args[0], args[1], args[2], args[3]);
            case 5: return args -> ((Func5A)func).call(inst, args[0], args[1], args[2], args[3], args[4]);
            case 6: return args -> ((Func6A)func).call(inst, args[0], args[1], args[2], args[3], args[4], args[5]);
            default: throw new IllegalArgumentException("Too many arguments");
        }
    }


    /**
     * Try to compile calls to lambdas, if fail use reflection
     */
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
        public TsonFunc compileField(Object inst, String name) {
            try {
                Field field = inst.getClass().getField(name);
                //don`t compile final field, just return it
                if(Modifier.isFinal(field.getModifiers())){
                    return constField(field.get(inst));
                }
                return makeFunc(inst, FuncCompiler.compile(field));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }


        @Override
        public TsonFunc.Compiler fork() {
            Compiler compiler = new Compiler();
            compiler.setEngine(getEngine());
            return compiler;
        }
    }
}