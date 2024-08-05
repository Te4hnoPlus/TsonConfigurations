package plus.tson.utl;


/**
 * Mutable version of Tuple
 */
public class TupleMutable<A, B> {
    public A A;
    public B B;

    public TupleMutable(){}

    public TupleMutable(A a, B b) {
        A = a;
        B = b;
    }

    public TupleMutable<A,B> edit(A a, B b){
        this.A = a;this.B = b;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple)) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        if (!A.equals(tuple.A)) return false;
        return B.equals(tuple.B);
    }


    @Override
    public int hashCode() {
        return 31 * A.hashCode() + B.hashCode();
    }


    @Override
    public String toString() {
        return "("+A+", "+B+")";
    }
}
