package plus.tson;

import plus.tson.utl.FuncCompiler;
import plus.tson.utl.Te4HashMap;
import plus.tson.utl.Te4HashSet;
import javax.script.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * Main Tson function interface
 */
public interface TsonFunc {
    /**
     * Default function compiler
     */
    FuncCompiler.Compiler COMPILER = new FuncCompiler.Compiler();


    /**
     * Call this functions with arguments
     * Default compiler don`t support more than 6 arguments
     */
    Object call(Object... args);


    /**
     * Call this without arguments
     */
    default Object call(){return call(ReflectField.EMPTY);}


    /**
     * @return Count of arguments or -1 if unknown
     */
    default int countArgs(){return -1;}


    /**
     * @return Array of names function arguments
     */
    default String[] args(){return Frame.EMPTY;}


    /**
     * @return True if this function hasn't 'inst' argument
     */
    default boolean isStatic(){return false;}


    /**
     * Unwrap functions. If can`t unwrap, return this
     */
    default Object unwrap(){return this;}


    /**
     * Frame to store code for compilation on script engine
     */
    class Frame implements CharSequence{
        private static final String[] EMPTY = new String[0];
        private final byte[] buffer;
        private int from, to;
        private final String[] args;
        private Object inst;
        private String cachedStr;

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


        /**
         * Trim self bounds if can, and return self
         */
        public Frame trim(){
            byte[] data = this.buffer;
            int from = this.from, to = this.to;
            while (from < to && isEmpty(data[from])){
                ++from;
            }
            while (from < to && isEmpty(data[to])){
                --to;
            }
            if(this.from == from && this.to == to)return this;
            cachedStr = null;
            this.from = from;
            this.to = to;
            return this;
        }


        private static boolean isEmpty(byte chr){
            return chr == ' '|| chr == '\n' || chr == '\t' || chr == '\r';
        }


        @Override
        public int length() {
            return to-from;
        }

        @Override
        public char charAt(int index) {
            return (char) buffer[from+index];
        }


        @Override
        public TsonFunc.Frame subSequence(int start, int end) {
            Frame frame = new Frame(inst, buffer, from + start, from + end, args);
            frame.cachedStr = cachedStr;
            frame.inst      = inst;
            return frame;
        }


        @Override
        public String toString(){
            if(cachedStr != null)return cachedStr;
            return cachedStr = new String(buffer, from, to-from, StandardCharsets.UTF_8);
        }


        /**
         * @return Object instance
         */
        public Object getInst(){
            return inst;
        }


        /**
         * Edit Object instance
         */
        public void setInst(Object inst) {
            this.inst = inst;
        }


        /**
         * @return true if function has 'inst' argument
         */
        public boolean hasInst(){
            for (String arg: args){
                if(arg.equals("inst"))return true;
            }
            return false;
        }


        /**
         * @return Array of names function arguments
         */
        public String[] getArgs(){
            return args;
        }
    }


    interface Field extends TsonFunc{
        /**
         * Access to field should be called without arguments
         */
        default Object call(Object... args){
            if(args.length != 0)throw new IllegalArgumentException();
            return call();
        }


        Object call();


        default int countArgs() {
            return 0;
        }
    }


    /**
     * Base interface for function compiler
     */
    interface Compiler{
        /**
         * Try to compile Object method to TsonFunc
         */
        TsonFunc compile(Object inst, String name);

        /**
         * Try to compile static function to TsonFunc
         */
        TsonFunc compile(Class<?> clazz, String name);

        /**
         * Try to compile field accessor as TsonFunc
         */
        TsonFunc compileField(Object inst, String name);

        /**
         * Try to compile code frame using script engine as TsonFunc
         */
        TsonFunc compile(Frame frame);

        /**
         * @return Copy of this compiler
         */
        default Compiler fork(){throw new UnsupportedOperationException();}
    }


    /**
     * Base function 'compiler'
     * This don`t compile any code, this use only reflections
     */
    class Reflector implements Compiler {
        private ScriptEngine engine;

        public void setEngine(ScriptEngine engine){
            this.engine = engine;
        }

        public ScriptEngine getEngine() {
            return engine;
        }


        public void tryInstallEngine(String name){
            ScriptEngineManager factory = new ScriptEngineManager();
            ScriptEngine engine = factory.getEngineByName(name);

            if(engine == null){
                List<ScriptEngineFactory> factories = factory.getEngineFactories();
                if(factories.size() == 0){
                    throw new RuntimeException("No engine found");
                }
                engine = factories.get(0).getScriptEngine();
                System.out.println("Selected engine ["+name+"] not exists, used [" + engine.getFactory().getLanguageName()+"]");

                if(factories.size() > 1) {
                    String[] allNames = new String[factories.size()];

                    for (int i = 0; i < factories.size(); i++) {
                        allNames[i] = factories.get(i).getLanguageName();
                    }
                    System.out.println("Available engines: " + Arrays.toString(allNames));
                }
            }
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
            //don`t compile final field, just return it
            if(field.isFinal())return constField(field.call());
            return field;
        }


        protected TsonFunc constField(Object value){
            return new ReflectConst(value);
        }


        @Override
        public TsonFunc compile(Frame frame) {
            if(engine == null){
                tryInstallEngine("js");
            }
            if(engine instanceof Compilable){
                if(frame.hasInst() && frame.getInst() != null)return new CompiledScriptMethod(engine, frame);
                else return new CompiledScriptFunc(engine, frame);
            } else {
                if(frame.hasInst() && frame.getInst() != null)return new ScriptMethod(engine, frame);
                else return new ScriptFunc(engine, frame);
            }
        }


        @Override
        public Compiler fork() {
            Reflector copy = new Reflector();
            copy.engine = engine;
            return copy;
        }
    }
}


/**
 * Base class for Script Engine function
 */
abstract class ScriptFuncBase implements TsonFunc{
    final String[] args;

    ScriptFuncBase(Frame frame){
        this.args = frame.getArgs();
        frame.trim();
    }


    /**
     * Create binding of these arguments
     */
    public Bindings bind(Object[] args){
        if(args.length == 0)return new MapBindings();
//        if(args.length == 0)return EmptyBindings.INSTANCE;
//        if(args.length == 1)return new SingleBindings(this.args[0], args[0]);
        return new MapBindings(this.args, args);
    }


    @Override
    public int countArgs() {
        return args.length;
    }


    @Override
    public String[] args() {
        return args;
    }
}


/**
 * Runtime-compile (from code) script function
 */
class ScriptFunc extends ScriptFuncBase {
    private final ScriptEngine engine;
    private final String code;

    ScriptFunc(ScriptEngine engine, Frame frame) {
        super(frame);
        this.engine = engine;
        this.code = frame.toString();
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


/**
 * Compiled script function, used this if script engine implements Compilable
 */
class CompiledScriptFunc extends ScriptFuncBase {
    private final CompiledScript func;

    CompiledScriptFunc(ScriptEngine engine, Frame frame){
        super(frame);
        Compilable compilable = (Compilable) engine;
        try {
            func = compilable.compile(frame.toString());
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


/**
 * Runtime-compile (from code) 'method' script function for object instance
 */
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


/**
 * Compiled 'method' script function, used this if script engine implements Compilable
 */
class CompiledScriptMethod extends CompiledScriptFunc {
    private final Object inst;

    CompiledScriptMethod(ScriptEngine engine, Frame frame) {
        super(engine, frame);
        inst = frame.getInst();
    }

    /**
     * Add `object` instance to bindings
     */
    @Override
    public Bindings bind(Object[] args) {
        if(args.length == 0)return new SingleBindings("inst", inst);
        MapBindings bindings = new MapBindings(this.args, args);
        bindings.fput("inst", inst);
        return bindings;
    }
}


/**
 * Bindings for more arguments based on {@link Te4HashMap}
 */
final class MapBindings extends Te4HashMap<String,Object> implements Bindings{
    public MapBindings(){}

    public MapBindings(String[] names, Object[] args) {
        super(names.length);
        if(args.length != names.length)throw new IllegalArgumentException("Args length must be " + args.length + " "+ Arrays.toString(args));
        for (int i = 0; i < args.length; i++) {
            fput(names[i], args[i]);
        }
    }
}


/**
 * Empty bindings, used if there are no arguments
 */
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
        return Collections.emptySet();
    }


    @Override
    public Collection<Object> values() {
        return Collections.emptyList();
    }


    @Override
    public Set<Entry<String, Object>> entrySet() {
        return Collections.emptySet();
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


/**
 * Bindings for one pair `var` -> `value`
 */
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
        if(value == null)return Collections.emptySet();
        Te4HashSet<String> set = new Te4HashSet<>();
        set.add(key);
        return set;
    }


    @Override
    public Collection<Object> values() {
        if(value == null)return Collections.emptyList();
        ArrayList<Object> list = new ArrayList<>(1);
        list.add(value);
        return list;
    }


    @Override
    public Set<Entry<String, Object>> entrySet() {
        Te4HashSet<Entry<String, Object>> set = new Te4HashSet<>(3);
        set.add(this);
        return set;
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


/**
 * Reflect `object` method
 */
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


/**
 * Static reflect function
 */
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


/**
 * Reflect accessor to `object` field
 */
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

    /**
     * @return `true` if field is final
     */
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


/**
 * Accessor for constant value
 */
class ReflectConst implements TsonFunc.Field{
    private final Object value;

    ReflectConst(Object value) {
        this.value = value;
    }


    @Override
    public Object call() {
        return value;
    }


    @Override
    public boolean isStatic() {
        return true;
    }
}