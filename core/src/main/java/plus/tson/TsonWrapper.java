package plus.tson;

import java.util.IdentityHashMap;
import java.util.function.Function;


/**
 * Wrapper for Tson objects
 */
public final class TsonWrapper extends IdentityHashMap<Class, Function<Object, TsonObj>> {
    private static final TsonWrapper INST = new TsonWrapper();

    private TsonWrapper() {
        super();
        Function<Object, TsonObj> temp = o -> new TsonInt((int) o);
        put(int.class    , temp);
        put(Integer.class, temp);

        temp = o -> new TsonLong((long) o);

        put(long.class, temp);
        put(Long.class, temp);

        temp = o -> new TsonFloat((float) o);

        put(float.class, temp);
        put(Float.class, temp);

        temp = o -> new TsonDouble((double) o);

        put(double.class, temp);
        put(Double.class, temp);

        temp = o -> TsonBool.of((boolean) o);

        put(boolean.class, temp);
        put(Boolean.class, temp);

        put(String.class, o -> new TsonStr((String) o));
    }


    /**
     * Wrap Object to Tson if necessary
     */
    public static TsonObj wrap(Object obj) {
        if(obj == null) return null;
        if(obj instanceof TsonObj) return (TsonObj) obj;
        Function<Object, TsonObj> res = INST.get(obj.getClass());
        if (res == null) return new TsonField<>(obj);
        return res.apply(obj);
    }
}