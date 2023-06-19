package plus.tson.utl;

import java.util.Objects;


public class ConstGetter<T,R> implements IGetter<T,R>{
    private final R r;
    public ConstGetter(R r) {
        this.r = r;
    }

    @Override
    public final R get(T t) {
        return r;
    }

    @Override
    public final boolean isConst() {
        return true;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstGetter<?, ?> that = (ConstGetter<?, ?>) o;
        return Objects.equals(r, that.r);
    }


    @Override
    public int hashCode() {
        return r != null ? r.hashCode() : 0;
    }
}