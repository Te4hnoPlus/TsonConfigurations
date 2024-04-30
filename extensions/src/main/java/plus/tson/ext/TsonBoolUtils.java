package plus.tson.ext;

import plus.tson.TsonMap;
import plus.tson.TsonObj;


/**
 * Utilities for working with boolean in configurations.
 * <pre>{@code
 *  True,  T, Yes, Y, V, 1, +  -> true
 *  False, F, No,  N, X, 0, -  -> false
 * }</pre>
 */
public class TsonBoolUtils {
    /**
     * @param src User data
     * @param key Key for access to TsonMap
     * @param def Default value
     */
    public static boolean calc(TsonMap src, String key, boolean def){
        return calc(src.get(key), def);
    }


    static boolean calc(TsonObj res, boolean def){
        if(res==null)return def;
        if(res.isNumber())return res.getInt() > 0;
        if(res.isString())return bool(res.getStr(), def);
        throw new IllegalArgumentException();
    }


    /**
     * @return Nearest similar value or default
     */
    public static boolean bool(String res, boolean def){
        switch (res.toLowerCase()) {
            case "t", "y", "v", "+", "true", "yes", "1": return true;
            case "f", "x", "n", "-", "false", "no", "0": return false;
            case "invert"                              : return !def;
        }
        return def;
    }
}