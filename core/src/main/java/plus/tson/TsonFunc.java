package plus.tson;

import plus.tson.utl.FuncCompiler;
import plus.tson.utl.Te4HashMap;
import javax.script.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;


public interface TsonFunc {
    Compiler COMPILER = new FuncCompiler.Compiler();


    Object call(Object... args);


    default Object call(){return call(ReflectField.EMPTY);}


    class Frame{
        private static final String[] EMPTY = new String[0];
        private final byte[] buffer;
        private int from, to;
        private final String[] args;
        private final Object inst;

        public Frame(Object inst, byte[] buffer, int from, int i, String... args) {
            this.inst = inst;
            this.buffer = buffer;
            this.from = from;
            to = i;
            if(args.length == 0){
                this.args = EMPTY;
            } else {
                this.args = args;
            }
        }


        public Frame trim(){
            byte[] data = this.buffer;
            int from = this.from, to = this.to;
            while (from < to && isEmpty(data[from])){
                ++from;
            }
            while (from < to && isEmpty(data[to])){
                --to;
            }
            this.from = from;
            this.to = to;
            return this;
        }


        private static boolean isEmpty(byte chr){
            return chr == ' '|| chr == '\n' || chr == '\t' || chr == '\r';
        }


        public String getCodeStr(){
            return new String(buffer, from, to-from, StandardCharsets.UTF_8);
        }


        public Object getInst(){
            return inst;
        }


        public String[]  getArgs(){
            return args;
        }
    }


    interface Field extends TsonFunc{
        default Object call(Object... args){
            if(args.length != 0)throw new IllegalArgumentException();
            return call();
        }
        Object call();
    }


    interface Compiler{
        TsonFunc compile(Object inst, String name);
        TsonFunc compile(Class<?> clazz, String name);
        TsonFunc compileField(Object inst, String name);
        TsonFunc compile(Frame frame);
    }


    class Reflector implements Compiler {
        private ScriptEngine engine;

        public void setEngine(ScriptEngine engine){
            this.engine = engine;
        }

        @Override
        public TsonFunc compile(Object inst, String name) {
            return new ReflectInstance(inst, name);
        }

        @Override
        public TsonFunc compile(Class<?> clazz, String name) {
            return new ReflectStatic(clazz, name);
        }

        @Override
        public TsonFunc compileField(Object inst, String name){
            ReflectField field = new ReflectField(inst, name);
            if(field.isFinal())return new ReflectConst(field.call());
            return field;
        }

        @Override
        public TsonFunc compile(Frame frame) {
            if(engine == null){
                ScriptEngineManager factory = new ScriptEngineManager();

                engine = factory.getEngineByName("js");
                if(engine == null){
                    List<ScriptEngineFactory> factories = factory.getEngineFactories();
                    if(factories.size() == 0){
                        throw new RuntimeException("No engine found");
                    }
                    engine = factories.get(0).getScriptEngine();
                    System.out.println("Selected engine: " + engine.getFactory().getLanguageName());
                }
            }
            if(engine instanceof Compilable){
                return new CompiliableScriptEngineFunc(engine, frame);
            } else {
                return new ScriptEngineFunc(engine, frame);
            }
        }
    }
}


class CompiliableScriptEngineFunc implements TsonFunc{
    private final CompiledScript func;
    private final String[] args;

    CompiliableScriptEngineFunc(ScriptEngine engine, Frame frame){
        Compilable compilable = (Compilable) engine;
        try {
            func = compilable.compile(frame.trim().getCodeStr());
            args = frame.getArgs();
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public Object call(Object... args) {
        try {
            return func.eval(new MapBindings(this.args, args));
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
}


class ScriptEngineFunc implements TsonFunc{
    private final ScriptEngine engine;
    private final String code;
    private final String[] args;

    ScriptEngineFunc(ScriptEngine engine, Frame frame) {
        this.engine = engine;
        this.code = frame.trim().getCodeStr();
        args = frame.getArgs();
    }


    @Override
    public Object call(Object... args) {
        if(args.length == 0){
            if(args.length != 0)throw new IllegalArgumentException("Args length must be 0");
            try {
                return engine.eval(code);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return engine.eval(code, new MapBindings(this.args, args));
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
        }
    }
}


class MapBindings extends Te4HashMap<String,Object> implements Bindings{
    public MapBindings(String[] names, Object[] args) {
        super(names.length);
        if(args.length != names.length)throw new IllegalArgumentException("Args length must be " + args.length + " "+ Arrays.toString(args));
        for (int i = 0; i < args.length; i++) {
            fput(names[i], args[i]);
        }
    }
}


class ReflectInstance implements TsonFunc{
    private final Object inst;
    private final String name;
    ReflectInstance(Object inst, String name) {
        this.inst = inst;
        this.name = name;
    }

    @Override
    public Object call(Object... args) {
        try {
            return TsonClass.invoke(inst.getClass(), inst, name, null, args);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}


class ReflectStatic implements TsonFunc{
    private final Class<?> clazz;
    private final String name;
    ReflectStatic(Class<?> clazz, String name) {
        this.clazz = clazz;
        this.name = name;
    }

    @Override
    public Object call(Object... args) {
        try {
            return TsonClass.invoke(clazz, null, name, args);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}


class ReflectField implements TsonFunc.Field{
    static final Object[] EMPTY = new Object[0];
    private final java.lang.reflect.Field field;
    private final Object inst;

    ReflectField(Object inst, String name) {
        try {
            this.inst = inst;
            this.field = inst.getClass().getField(name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    boolean isFinal(){
        return Modifier.isFinal(field.getModifiers());
    }


    @Override
    public Object call() {
        try {
            return field.get(inst);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}


class ReflectConst implements TsonFunc.Field{
    private final Object value;

    ReflectConst(Object value) {
        this.value = value;
    }

    @Override
    public Object call() {
        return value;
    }
}