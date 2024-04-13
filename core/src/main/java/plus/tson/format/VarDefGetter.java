package plus.tson.format;

import java.util.Objects;


public class VarDefGetter<T> implements IVarGetter<T> {
    private final IVarGetter<T> parent;
    private final IVarGetter<T> def;

    public VarDefGetter(IVarGetter<T> parent, IVarGetter<T> def) {
        this.parent = parent;
        this.def = def;
    }


    @Override
    public String get(T t){
        String result = parent.get(t);
        if(result==null)return def.get(t);
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VarDefGetter<?> that = (VarDefGetter<?>) o;
        if (!Objects.equals(parent, that.parent)) return false;
        return Objects.equals(def, that.def);
    }
}
