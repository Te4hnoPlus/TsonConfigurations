package plus.tson.ext;

import plus.tson.TsonMap;
import plus.tson.TsonObj;


/**
 * Utilities for working with boolean in configurations.
 * <pre>{@code
 *  True, Yes, Y, 1, +  -> true
 *  False, No, N, 0, -  -> false
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
        switch (name) {
            case "Y":
            case "y":
            case "+":
                return true;
            case "N":
            case "n":
            case "-":
                return false;
        }
        name = name.toLowerCase();

        switch (name) {
            case "true":
            case "yes":
                return true;
            case "false":
            case "no":
                return false;
        }
        return def;
    }
}