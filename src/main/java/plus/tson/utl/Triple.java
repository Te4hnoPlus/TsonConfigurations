package plus.tson.utl;


public class Triple<A,B,C> extends Tuple<A,B>{
    public final C C;

    public Triple(A a, B b, C c) {
        super(a, b);
        this.C = c;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
        return C.equals(triple.C);
    }


    @Override
    public int hashCode() {
        return 31 * super.hashCode() + C.hashCode();
    }
}