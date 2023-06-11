package plus.tson.utl;


public class Triple<A,B,C> extends Tuple<A,B>{
    public final C C;
    public Triple(A a, B b, C c) {
        super(a, b);
        this.C = c;
    }
}