package plus.tson.utl.uns;

public class UnsafeChecker {
    private static byte canUseJ21 = -1;

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