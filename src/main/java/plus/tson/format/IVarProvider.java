package plus.tson.format;


@FunctionalInterface
public interface IVarProvider<T> {
    IVarGetter<T> getFmGetter(String name);
}
