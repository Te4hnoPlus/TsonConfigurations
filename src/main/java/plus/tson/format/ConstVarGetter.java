package plus.tson.format;

import java.util.Objects;


public final class ConstVarGetter<T> implements IVarGetter<T> {
    private final String val;

    public ConstVarGetter(String val) {
        this.val = val;
    }


    @Override
    public String get(T o) {
        return val;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(val, ((ConstVarGetter<?>) o).val);
    }
}