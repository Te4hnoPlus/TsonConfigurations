package col;

import jdk.internal.misc.Unsafe;


public class Unsafe21 {
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    public static boolean compareAndSwap(Object ref, long offset, Object prev, Object cur){
        return UNSAFE.compareAndSetReference(ref, offset, prev, cur);
    }
}
