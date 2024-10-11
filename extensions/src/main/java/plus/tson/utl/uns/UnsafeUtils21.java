package plus.tson.utl.uns;

import jdk.internal.misc.Unsafe;
import static plus.tson.utl.uns.UnsafeUtils.strOffset;


/**
 * Unsafe utils, used when jdk.internal.misc.Unsafe is available
 */
public class UnsafeUtils21 {
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    /**
     * Atomically updates Java variable to {@code cur} if it is currently
     * holding {@code expected}.
     *
     * <p>This operation has memory semantics of a {@code volatile} read
     * and write.  Corresponds to C11 atomic_compare_exchange_strong. See {@link sun.misc.Unsafe#compareAndSwapObject}
     *
     * @return {@code true} if successful
     */
    public static boolean compareAndSwap(Object ref, long offset, Object prev, Object cur){
        return UNSAFE.compareAndSetReference(ref, offset, prev, cur);
    }


    /**
     * Atomically updates Java variable to {@code cur} if it is currently
     * holding {@code expected}.
     *
     * <p>This operation has memory semantics of a {@code volatile} read
     * and write.  Corresponds to C11 atomic_compare_exchange_strong. See {@link sun.misc.Unsafe#compareAndSwapInt}
     *
     * @return {@code true} if successful
     */
    public static boolean compareAndSwap(Object ref, long offset, int prev, int cur){
        return UNSAFE.compareAndSetInt(ref, offset, prev, cur);
    }


    /**
     * Unsafe create string without copy bytes
     */
    public static String stringOf(byte[] bytes) {
        try {
            String str = (String) UNSAFE.allocateInstance(String.class);
            UNSAFE.putReference(str, strOffset, bytes);
            return str;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @return reference to string bytes, {@link String#value}
     */
    public static byte[] getBytes(String str){
        return (byte[]) UNSAFE.getReference(str, strOffset);
    }
}
