package plus.tson.utl.uns;

import sun.misc.Unsafe;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


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
     * @return Offset of object field {@code name} in {@code clazz}
     */
    public static long offset(Class<?> clazz, String name){
        try {
            return UNSAFE.objectFieldOffset(clazz.getDeclaredField(name));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @return Offset of object/static field {@code name} in {@code clazz}
     */
    public static long offsetS(Class<?> clazz, String name){
        try {
            Field field = clazz.getDeclaredField(name);
            if(Modifier.isStatic(field.getModifiers()))
                return UNSAFE.staticFieldOffset(field);
            return UNSAFE.objectFieldOffset(field);
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


    /**
     * Unsafe get object field value
     */
    public static <T> T get(Object src, long offSet){
        return (T) UNSAFE.getObject(src, offSet);
    }


    /**
     * Unsafe set object field value
     */
    public static void set(Object src, long offSet, Object value){
        UNSAFE.putObject(src, offSet, value);
    }


    /**
     * Unsafe allocate instance
     */
    public static <T> T newObj(Class<T> clazz){
        try {
            return (T) UNSAFE.allocateInstance(clazz);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}