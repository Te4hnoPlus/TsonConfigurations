package plus.tson.utl.uns;

import jdk.internal.misc.Unsafe;
import static plus.tson.utl.uns.UnsafeUtils.strOffset;


public class UnsafeUtils21 {
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    public static boolean compareAndSwap(Object ref, long offset, Object prev, Object cur){
        return UNSAFE.compareAndSetReference(ref, offset, prev, cur);
    }


    public static boolean compareAndSwap(Object ref, long offset, int prev, int cur){
        return UNSAFE.compareAndSetInt(ref, offset, prev, cur);
    }


    public static String stringOf(byte[] bytes) {
        try {
            String str = (String) UNSAFE.allocateInstance(String.class);
            UNSAFE.putReference(str, strOffset, bytes);
            return str;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }


    public static byte[] getBytes(String str){
        return (byte[]) UNSAFE.getReference(str, strOffset);
    }
}
