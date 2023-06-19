package plus.tson.format;


@FunctionalInterface
public interface IVarProvider<T> {
    IVarGetter<T> get(String name);
}
