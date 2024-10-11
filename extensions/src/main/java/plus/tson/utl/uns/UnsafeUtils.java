package plus.tson.utl.uns;

import sun.misc.Unsafe;
import java.lang.reflect.Field;


/**
 * Legacy unsafe utils, used when jdk.internal.misc.Unsafe is not available
 */
public class UnsafeUtils {
    //object reference size
    public static final int REF_SIZE;
    public static final int REF_SIZE_D2;
    public static final int REF_SIZE_M2;
    public static final Unsafe UNSAFE;
    //string `bytes` offset
    static final long strOffset;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);
            UNSAFE = (Unsafe) field.get(null);
            REF_SIZE = UNSAFE.addressSize();
            REF_SIZE_D2 = REF_SIZE>>1;
            REF_SIZE_M2 = REF_SIZE<<1;
            strOffset = offset(String.class, "value");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Atomically updates Java variable to {@code cur} if it is currently
     * holding {@code expected}.
     *
     * <p>This operation has memory semantics of a {@code volatile} read
     * and write.  Corresponds to C11 atomic_compare_exchange_strong. See {@link Unsafe#compareAndSwapObject}
     *
     * @return {@code true} if successful
     */
    public static boolean compareAndSwap(Object ref, long offset, Object prev, Object cur){
        return UNSAFE.compareAndSwapObject(ref, offset, prev, cur);
    }


    /**
     * Atomically updates Java variable to {@code cur} if it is currently
     * holding {@code expected}.
     *
     * <p>This operation has memory semantics of a {@code volatile} read
     * and write.  Corresponds to C11 atomic_compare_exchange_strong. See {@link Unsafe#compareAndSwapInt}
     *
     * @return {@code true} if successful
     */
    public static boolean compareAndSwap(Object ref, long offset, int prev, int cur){
        return UNSAFE.compareAndSwapInt(ref, offset, prev, cur);
    }


    /**
     * @return Offset of field {@code name} in {@code clazz}
     */
    public static long offset(Class<?> clazz, String name){
        try {
            return UNSAFE.objectFieldOffset(clazz.getDeclaredField(name));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Unsafe create string without copy bytes
     */
    public static String stringOf(byte[] bytes) {
        try {
            String str = (String) UNSAFE.allocateInstance(String.class);
            UNSAFE.putObject(str, strOffset, bytes);
            return str;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @return reference to string bytes, {@link String#value}
     */
    public static byte[] getBytes(String str){
        return (byte[]) UNSAFE.getObject(str, strOffset);
    }
}