package plus.tson.utl;


public interface IGetter<T,R> {
    R get(T t);
    default boolean isConst(){
        return false;
    }
}
