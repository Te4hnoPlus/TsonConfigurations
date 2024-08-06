package plus.tson.ext;

import plus.tson.TsonFunc;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.utl.FuncCompiler;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * Utility methods for building functions from Tson
 */
public class TsonFuncUtils {
    public static final Predicate ALWAYS_TRUE    = msg -> true;
    public static final Consumer  EMPTY_CONSUMER = msg -> {};
    public static final Function  EMPTY_FUNCTION = msg -> null;

    /**
     * Build consumer from Tson
     * @param obj Target object
     * @param factory Consumer factory
     * @param logFactory Predicate factory
     */
    public static <T> Consumer<T> consumerOf(TsonObj obj, Function<TsonObj, Consumer<T>> factory, Function<TsonObj, Predicate<T>> logFactory) {
        if(obj != null) {
            if (obj.isCustom()) return consumerOf(obj);
            if (obj.isMap()) {
                Consumer<T> result = consumerOfMap(obj.getMap(), factory, logFactory);
                if(result != null) return result;
            }
            if(obj.isList()){
                TsonList list = obj.getList();
                Consumer<T>[] consumers = new Consumer[list.size()];

                for(int i = 0; i < list.size(); i++) {
                    consumers[i] = consumerOf(list.get(i), factory, logFactory);
                }

                return msg -> {
                    for(Consumer<T> consumer : consumers) consumer.accept(msg);
                };
            }
            return factory.apply(obj);
        }
        return EMPTY_CONSUMER;
    }


    /**
     * Try to adapt custom object to consumer
     */
    private static <T> Consumer<T> consumerOf(TsonObj obj){
        if(!obj.isCustom())throw new IllegalArgumentException("Only custom objects is supported!");
        Object raw = obj.getField();
        if(raw instanceof Consumer<?>) return (Consumer<T>) raw;
        if(raw instanceof TsonFunc){
            TsonFunc func = (TsonFunc) raw;
            int count = func.countArgs();
            if(count == -1)return func::call;
            if(count != 1)throw new IllegalArgumentException("Args should be = 1");

            Object raw2 = func.unwrap();
            if(!(raw2 instanceof FuncCompiler.Func1A))return func::call;

            FuncCompiler.Func1A<T> rFunc = (FuncCompiler.Func1A<T>) raw2;
            return t -> rFunc.call(null, t);
        }
        throw new IllegalArgumentException("Unsupported type: "+obj);
    }


    /**
     * Build consumer from Tson Map, see {@link TsonFuncUtils#consumerOf(TsonObj, Function, Function)}
     */
    private static <T> Consumer<T> consumerOfMap(TsonMap map, Function<TsonObj, Consumer<T>> factory, Function<TsonObj, Predicate<T>> logFactory) {
        String type = map.getStr("type");
        if(type == null){
            if(map.containsKey("if")){
                type = "if";
            }
            if(type == null) return null;
        }

        switch (type){
            case "if":{
                TsonObj obj = map.get("if");
                Predicate<T> pred = logFactory.apply(obj);
                Consumer<T> then = consumerOf(map.get("then"), factory, logFactory);
                Consumer<T> els  = consumerOf(map.get("else"), factory, logFactory);
                return t -> {
                    if(pred.test(t)) then.accept(t);
                    else             els.accept(t);
                };
            }
        }
        return null;
    }


    /**
     * Build function from Tson
     * @param obj Target object
     * @param factory Function factory
     * @param logFactory Predicate factory
     */
    public static <T,R> Function<T,R> functionOf(TsonObj obj, Function<TsonObj, Function<T,R>> factory, Function<TsonObj, Predicate<T>> logFactory) {
        if(obj != null) {
            if (obj.isCustom()) return functionOf(obj);
            if (obj.isMap()) {
                Function<T,R> result = functionOfMap(obj.getMap(), factory, logFactory);
                if(result != null) return result;
            }
            return factory.apply(obj);
        }
        return EMPTY_FUNCTION;
    }


    /**
     * Try to adapt custom object to function
     */
    private static <T,R> Function<T,R> functionOf(TsonObj obj){
        if(!obj.isCustom())throw new IllegalArgumentException("Only custom objects is supported!");
        Object raw = obj.getField();
        if(raw instanceof Function<?,?>) return (Function<T, R>) raw;
        if(raw instanceof TsonFunc){
            TsonFunc func = (TsonFunc) raw;
            int count = func.countArgs();
            if(count == -1)return t -> (R) func.call(t);
            if(count != 1)throw new IllegalArgumentException("Args should be = 1");

            Object raw2 = func.unwrap();
            if(!(raw2 instanceof FuncCompiler.Func1A))return t -> (R) func.call(t);

            FuncCompiler.Func1A<R> rFunc = (FuncCompiler.Func1A<R>) raw2;
            return t -> rFunc.call(null, t);
        }
        throw new IllegalArgumentException("Unsupported type: "+obj);
    }


    /**
     * Build consumer from Tson Map, see {@link TsonFuncUtils#functionOf(TsonObj, Function, Function)}
     */
    private static <T,R> Function<T,R> functionOfMap(TsonMap map, Function<TsonObj, Function<T,R>> factory, Function<TsonObj, Predicate<T>> logFactory) {
        String type = map.getStr("type");
        if(type == null){
            if(map.containsKey("if")){
                type = "if";
            }
            if(type == null) return null;
        }

        switch (type){
            case "if":{
                TsonObj obj = map.get("if");
                Predicate<T> pred = logFactory.apply(obj);
                Function<T,R> then = functionOf(map.get("then"), factory, logFactory);
                Function<T,R> els  = functionOf(map.get("else"), factory, logFactory);
                return t -> {
                    if(pred.test(t))return then.apply(t);
                    else            return els.apply(t);
                };
            }
        }
        return null;
    }


    /**
     * Build predicate from Tson
     * @param obj Target object
     * @param factory Predicate factory
     */
    public static <T> Predicate<T> predicateOf(TsonObj obj, Function<TsonObj, Predicate<T>> factory) {
        if(obj != null) {
            if (obj.isCustom()) return predicateOf(obj);
            if (obj.isMap()) {
                Predicate<T> result = predicateOfMap(obj.getMap(), factory);
                if(result != null) return result;
            }
            return factory.apply(obj);
        }
        return ALWAYS_TRUE;
    }


    /**
     * Try to adapt custom object to predicate
     */
    private static <T> Predicate<T> predicateOf(TsonObj obj){
        if(!obj.isCustom())throw new IllegalArgumentException("Only custom objects is supported!");
        Object raw = obj.getField();
        if(raw instanceof Predicate<?>) return (Predicate<T>) raw;
        if(raw instanceof TsonFunc){
            TsonFunc func = (TsonFunc) raw;
            int count = func.countArgs();
            if(count == -1)return t -> (boolean)func.call(t);
            if(count != 1)throw new IllegalArgumentException("Args should be = 1");

            Object raw2 = func.unwrap();
            if(!(raw2 instanceof FuncCompiler.Func1A))return t -> (boolean)func.call(t);

            FuncCompiler.Func1A<Boolean> rFunc = (FuncCompiler.Func1A<Boolean>) raw2;
            return t -> rFunc.call(null, t);
        }
        throw new IllegalArgumentException("Unsupported type: "+obj);
    }


    /**
     * Build predicate from List, see {@link TsonFuncUtils#predicateOf(TsonObj, Function)}
     */
    private static <T> Predicate<T>[] predicatesOfList(TsonList list, Function<TsonObj, Predicate<T>> factory) {
        final Predicate<T>[] parents = new Predicate[list.size()];
        for(int i = 0; i < list.size(); i++) {
            parents[i] = predicateOf(list.get(i), factory);
        }
        return parents;
    }


    /**
     * Build predicate from Tson Map, see {@link TsonFuncUtils#predicateOf(TsonObj, Function)}
     */
    private static <T> Predicate<T> predicateOfMap(TsonMap map, Function<TsonObj, Predicate<T>> factory) {
        String type = map.getStr("type");
        if(type == null){
            if(map.containsKey("or")){
                type = "or";
            }
            else if(map.containsKey("and")){
                type = "and";
            }
            else if(map.containsKey("not")){
                type = "not";
            }
            if(type == null) return null;
        }
        switch (type){
            case "or":{
                final Predicate<T>[] parents = predicatesOfList(map.getList("or"), factory);
                if(parents.length == 0)return ALWAYS_TRUE;
                if(parents.length == 1)return parents[0];
                if(parents.length == 2)return parents[0].or(parents[1]);
                return msg -> {
                    for(Predicate<T> parent : parents) {
                        if(parent.test(msg)) return true;
                    }
                    return false;
                };
            }
            case "and":{
                final Predicate<T>[] parents = predicatesOfList(map.getList("and"), factory);
                if(parents.length == 0) return ALWAYS_TRUE;
                if(parents.length == 1) return parents[0];
                if(parents.length == 2) return parents[0].and(parents[1]);
                return msg -> {
                    for(Predicate<T> parent : parents) {
                        if(!parent.test(msg)) return false;
                    }
                    return true;
                };
            }
            case "not":{
                final Predicate<T> parent = predicateOf(map.get("not"), factory);
                return msg -> !parent.test(msg);
            }
        }
        return null;
    }
}