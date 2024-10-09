package col;

import sun.misc.Unsafe;
import java.lang.reflect.Field;


public class UnsafeUtils {
    public static final int REF_SIZE;
    public static final int REF_SIZE_D2;
    public static final int REF_SIZE_M2;
    public static final Unsafe UNSAFE;
    private static final long strOffset;

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


    public static boolean compareAndSwap(Object ref, long offset, Object prev, Object cur){
        return UNSAFE.compareAndSwapObject(ref, offset, prev, cur);
    }


    public static long offset(Class<?> clazz, String name){
        try {
            return UNSAFE.objectFieldOffset(clazz.getDeclaredField(name));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }


    public static String stringOf(byte[] bytes) {
        try {
            String str = (String) UNSAFE.allocateInstance(String.class);
            UNSAFE.putObject(str, strOffset, bytes);
            return str;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }


    public static byte[] getBytes(String str){
        return (byte[]) UNSAFE.getObject(str, strOffset);
    }
}