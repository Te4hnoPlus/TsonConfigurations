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


        public boolean hasInst(){
            if(inst == null)return false;
            for (String arg: args){
                if(arg.equals("inst"))return true;
            }
            return false;
        }


        public String[] getArgs(){
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
                if(frame.hasInst())return new CompiledScriptMethod(engine, frame);
                else return new CompiledScriptFunc(engine, frame);
            } else {
                if(frame.hasInst())return new ScriptMethod(engine, frame);
                else return new ScriptFunc(engine, frame);
            }
        }
    }
}


abstract class ScriptFuncBase implements TsonFunc{
    final String[] args;

    ScriptFuncBase(Frame frame){
        this.args = frame.getArgs();
        frame.trim();
    }


    public Bindings bind(Object[] args){
        if(args.length == 0)return EmptyBindings.INSTANCE;
        if(args.length == 1)return new SingleBindings(this.args[0], args[0]);
        return new MapBindings(this.args, args);
    }
}


class CompiledScriptFunc extends ScriptFuncBase {
    private final CompiledScript func;

    CompiledScriptFunc(ScriptEngine engine, Frame frame){
        super(frame);
        Compilable compilable = (Compilable) engine;
        try {
            func = compilable.compile(frame.getCodeStr());
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public Object call(Object... args) {
        try {
            return func.eval(bind(args));
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
}


class CompiledScriptMethod extends CompiledScriptFunc {
    private final Object inst;

    CompiledScriptMethod(ScriptEngine engine, Frame frame) {
        super(engine, frame);
        inst = frame.getInst();
    }

    @Override
    public Bindings bind(Object[] args) {
        if(args.length == 0)return new SingleBindings("inst", inst);
        MapBindings bindings = new MapBindings(this.args, args);
        bindings.fput("inst", inst);
        return bindings;
    }
}


class ScriptFunc extends ScriptFuncBase {
    private final ScriptEngine engine;
    private final String code;

    ScriptFunc(ScriptEngine engine, Frame frame) {
        super(frame);
        this.engine = engine;
        this.code = frame.getCodeStr();
    }


    @Override
    public Object call(Object... args) {
        try {
            return engine.eval(code, bind(args));
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
}


class ScriptMethod extends ScriptFunc {
    private final Object inst;

    ScriptMethod(ScriptEngine engine, Frame frame) {
        super(engine, frame);
        inst = frame.getInst();
    }


    @Override
    public Bindings bind(Object[] args) {
        if(args.length == 0)return new SingleBindings("inst", inst);
        MapBindings bindings = new MapBindings(this.args, args);
        bindings.fput("inst", inst);
        return bindings;
    }
}


final class MapBindings extends Te4HashMap<String,Object> implements Bindings{
    public MapBindings(String[] names, Object[] args) {
        super(names.length);
        if(args.length != names.length)throw new IllegalArgumentException("Args length must be " + args.length + " "+ Arrays.toString(args));
        for (int i = 0; i < args.length; i++) {
            fput(names[i], args[i]);
        }
    }
}


final class EmptyBindings implements Bindings{
    static final EmptyBindings INSTANCE = new EmptyBindings();
    private EmptyBindings(){}
    @Override
    public Object put(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ?> toMerge) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {}

    @Override
    public Set<String> keySet() {
        return Set.of();
    }

    @Override
    public Collection<Object> values() {
        return List.of();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return Set.of();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Object get(Object key) {
        return null;
    }

    @Override
    public Object remove(Object key) {
        return null;
    }
}


final class SingleBindings implements Bindings, Map.Entry<String, Object> {
    private String key;
    private Object value;

    SingleBindings(){}
    SingleBindings(String key, Object value){
        this.key = key;
        this.value = value;
    }

    @Override
    public Object put(String name, Object value) {
        if(name.equals(key)){
            Object prev = this.value;
            this.value = value;
            return prev;
        }
        else throw new IllegalArgumentException("Can't set "+name+" to "+value+" because it's not "+key);
    }


    @Override
    public void putAll(Map<? extends String, ?> toMerge) {
        toMerge.forEach(this::put);
    }


    @Override
    public void clear() {
        value = null;
    }


    @Override
    public Set<String> keySet() {
        return Set.of(key);
    }

    @Override
    public Collection<Object> values() {
        return List.of(value);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return Set.of(this);
    }

    @Override
    public int size() {
        if(value == null)return 0;
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return value == null;
    }

    @Override
    public boolean containsKey(Object key) {
        return this.key.equals(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return Objects.equals(this.value, value);
    }

    @Override
    public Object get(Object key) {
        if(key.equals(this.key))return this.value;
        return null;
    }


    @Override
    public Object remove(Object key) {
        if(key.equals(this.key)){
            Object prev = this.value;
            this.value = null;
            return prev;
        }
        return null;
    }


    @Override
    public String getKey() {
        return key;
    }


    @Override
    public Object getValue() {
        return value;
    }


    @Override
    public Object setValue(Object value) {
        Object prev = this.value;
        this.value = value;
        return prev;
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