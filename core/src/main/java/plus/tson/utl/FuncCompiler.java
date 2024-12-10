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
        int count = mtd.getParameterCount();
        if(Modifier.isStatic(mtd.getModifiers())){
            count -= 1;
        }
        return compile(mtd, srcOfCount(count));
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
            case -1:return FuncS0A.class;
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
     * Base wrapped FuncCompiler`s lambda functions
     */
    private static abstract class TFuncBase implements TsonFunc{
        final Object inst;

        private TFuncBase(Object inst) {
            this.inst = inst;
        }


        @Override
        public int countArgs() {
            return 0;
        }
    }
    /**
     * Wrapped FuncCompiler`s lambda functions
     */
    private static final class TFunc0A extends TFuncBase{
        private final Func0A<?> parent;
        private TFunc0A(Object inst, Func0A<?> parent) {
            super(inst);
            this.parent = parent;
        }
        @Override
        public Object call() {
            return parent.call(inst);
        }
        @Override
        public Object call(Object... args) {
            return parent.call(inst);
        }

        @Override
        public Object unwrap() {
            return parent;
        }
    }
    private static final class TFunc1A extends TFuncBase{
        private final Func1A<?> parent;
        private TFunc1A(Object inst, Func1A<?> parent) {
            super(inst);
            this.parent = parent;
        }
        @Override
        public Object call(Object... args) {
            return parent.call(inst, args[0]);
        }
        @Override
        public int countArgs() {
            return 1;
        }
        @Override
        public Object unwrap() {
            return parent;
        }
    }
    private static final class TFunc2A extends TFuncBase{
        private final Func2A<?> parent;
        private TFunc2A(Object inst, Func2A<?> parent) {
            super(inst);
            this.parent = parent;
        }
        @Override
        public Object call(Object... args) {
            return parent.call(inst, args[0], args[1]);
        }
        @Override
        public int countArgs() {
            return 2;
        }
        @Override
        public Object unwrap() {
            return parent;
        }
    }
    private static final class TFunc3A extends TFuncBase{
        private final Func3A<?> parent;
        private TFunc3A(Object inst, Func3A<?> parent) {
            super(inst);
            this.parent = parent;
        }
        @Override
        public Object call(Object... args) {
            return parent.call(inst, args[0], args[1], args[2]);
        }
        @Override
        public int countArgs() {
            return 3;
        }

        @Override
        public Object unwrap() {
            return parent;
        }
    }
    private static final class TFunc4A extends TFuncBase{
        private final Func4A<?> parent;
        private TFunc4A(Object inst, Func4A<?> parent) {
            super(inst);
            this.parent = parent;
        }
        @Override
        public Object call(Object... args) {
            return parent.call(inst, args[0], args[1], args[2], args[3]);
        }
        @Override
        public int countArgs() {
            return 4;
        }
        @Override
        public Object unwrap() {
            return parent;
        }
    }
    private static final class TFunc5A extends TFuncBase{
        private final Func5A<?> parent;
        private TFunc5A(Object inst, Func5A<?> parent) {
            super(inst);
            this.parent = parent;
        }
        @Override
        public Object call(Object... args) {
            return parent.call(inst, args[0], args[1], args[2], args[3], args[4]);
        }
        @Override
        public int countArgs() {
            return 5;
        }
        @Override
        public Object unwrap() {
            return parent;
        }
    }
    private static final class TFunc6A extends TFuncBase{
        private final Func6A<?> parent;
        private TFunc6A(Object inst, Func6A<?> parent) {
            super(inst);
            this.parent = parent;
        }
        @Override
        public Object call(Object... args) {
            return parent.call(inst, args[0], args[1], args[2], args[3], args[4], args[5]);
        }
        @Override
        public int countArgs() {
            return 6;
        }
        @Override
        public Object unwrap() {
            return parent;
        }
    }


    /**
     * Base wrapped FuncCompiler`s static lambda functions
     */
    private interface SFuncBase extends TsonFunc{
        @Override
        default boolean isStatic() {
            return true;
        }
    }


    /**
     * Wrapped FuncCompiler`s static lambda functions
     */
    private static final class SFunc0A implements SFuncBase{
        private final FuncS0A<?> parent;
        private SFunc0A(FuncS0A<?> parent) {
            this.parent = parent;
        }
        @Override
        public Object call() {
            return parent.call();
        }
        @Override
        public Object call(Object... args) {
            return parent.call();
        }
        @Override
        public int countArgs() {
            return 0;
        }
    }
    private static final class SFunc1A implements SFuncBase{
        private final Func0A<?> parent;
        private SFunc1A(Func0A<?> parent) {
            this.parent = parent;
        }
        @Override
        public Object call(Object... args) {
            return parent.call(args[0]);
        }
        @Override
        public int countArgs() {
            return 1;
        }
    }
    private static final class SFunc2A implements SFuncBase{
        private final Func1A<?> parent;
        private SFunc2A(Func1A<?> parent) {
            this.parent = parent;
        }
        @Override
        public Object call(Object... args) {
            return parent.call(args[0], args[1]);
        }
        @Override
        public int countArgs() {
            return 2;
        }
    }
    private static final class SFunc3A implements SFuncBase{
        private final Func2A<?> parent;
        private SFunc3A(Func2A<?> parent) {
            this.parent = parent;
        }
        @Override
        public Object call(Object... args) {
            return parent.call(args[0], args[1], args[2]);
        }
        @Override
        public int countArgs() {
            return 3;
        }
    }
    private static final class SFunc4A implements SFuncBase{
        private final Func3A<?> parent;
        private SFunc4A(Func3A<?> parent) {
            this.parent = parent;
        }
        @Override
        public Object call(Object... args) {
            return parent.call(args[0], args[1], args[2], args[3]);
        }
        @Override
        public int countArgs() {
            return 4;
        }
    }
    private static final class SFunc5A implements SFuncBase{
        private final Func4A<?> parent;
        private SFunc5A(Func4A<?> parent) {
            this.parent = parent;
        }
        @Override
        public Object call(Object... args) {
            return parent.call(args[0], args[1], args[2], args[3], args[4]);
        }
        @Override
        public int countArgs() {
            return 5;
        }
    }
    private static final class SFunc6A implements SFuncBase{
        private final Func5A<?> parent;
        private SFunc6A(Func5A<?> parent) {
            this.parent = parent;
        }
        @Override
        public Object call(Object... args) {
            return parent.call(args[0], args[1], args[2], args[3], args[4], args[5]);
        }
        @Override
        public int countArgs() {
            return 5;
        }
    }


    /**
     * Wrap raw FuncCompiler`s function to TsonFunc
     */
    public static TsonFunc makeFunc(Object inst, Object func){
        if(inst == null)return makeFunc(func);
        int count = countArgs(func);
        switch (count){
            case 0: return new TFunc0A(inst, (Func0A)func);
            case 1: return new TFunc1A(inst, (Func1A)func);
            case 2: return new TFunc2A(inst, (Func2A)func);
            case 3: return new TFunc3A(inst, (Func3A)func);
            case 4: return new TFunc4A(inst, (Func4A)func);
            case 5: return new TFunc5A(inst, (Func5A)func);
            case 6: return new TFunc6A(inst, (Func6A)func);
            default: throw new IllegalArgumentException("Too many arguments");
        }
    }


    public static TsonFunc makeFunc(Object func){
        int count = countArgs(func);
        switch (count) {
            case 0: return new SFunc0A((FuncS0A<?>) func);
            case 1: return new SFunc1A((Func0A<?>) func);
            case 2: return new SFunc2A((Func1A<?>) func);
            case 3: return new SFunc3A((Func2A<?>) func);
            case 4: return new SFunc4A((Func3A<?>) func);
            case 5: return new SFunc5A((Func4A<?>) func);
            case 6: return new SFunc6A((Func5A<?>) func);
        }
        throw new IllegalArgumentException("Too many arguments");
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
                return makeFunc(FuncCompiler.compile(mtd));
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