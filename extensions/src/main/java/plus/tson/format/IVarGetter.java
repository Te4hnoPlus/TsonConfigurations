package plus.tson.format;


@FunctionalInterface
public interface IVarGetter<T> {
    String get(T t);
    default boolean isConst(){return this instanceof ConstVarGetter;}
    static boolean isConst(IVarGetter<?> g){
        return g instanceof ConstVarGetter;
    }
}
