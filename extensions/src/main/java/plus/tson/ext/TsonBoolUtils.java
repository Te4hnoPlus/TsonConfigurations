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
        TsonObj res = src.get(key);
        if(res==null)return def;
        if(res.isNumber())return res.getInt() != 0;
        if(res.isString())return bool(res.getStr(), def);
        throw new IllegalArgumentException();
    }


    public static boolean bool(String name, boolean def){
        name = name.toLowerCase();

        switch (name) {
            case "t", "y", "v", "+", "true", "yes":
                return true;
            case "f", "x", "n", "-", "false", "no":
                return false;
            case "invert":
                return !def;
        }
        return def;
    }
}