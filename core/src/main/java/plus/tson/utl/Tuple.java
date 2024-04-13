package plus.tson.utl;


public class Tuple<A, B> {
    public final A A;
    public final B B;

    public Tuple(A a, B b) {
        A = a;
        B = b;
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