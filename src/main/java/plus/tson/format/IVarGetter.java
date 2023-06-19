package plus.tson.format;


@FunctionalInterface
public interface IVarGetter<T> {
    String get(T t);
    static boolean isConst(IVarGetter<?> g){
        return g instanceof ConstVarGetter;
    }
}
