package plus.tson.utl.uns;

/**
 * Helpful class to check if jdk.internal.misc.Unsafe is available
 */
public class UnsafeChecker {
    // -1 - not checked, 0 - not available, 1 - available
    private static byte canUseJ21 = -1;

    /**
     * @return true if jdk.internal.misc.Unsafe is available
     */
    public static boolean checkSupports21(){
        byte state = canUseJ21;
        if(state == -1) {
            try {
                Class.forName("jdk.internal.misc.Unsafe").getDeclaredMethod("getUnsafe").invoke(null);
                canUseJ21 = 1;
                return true;
            } catch (Throwable e) {
                canUseJ21 = 0;
                return false;
            }
        }
        return state == 1;
    }
}