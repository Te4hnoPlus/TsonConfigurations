package plus.tson.ext;

import plus.tson.*;
import java.util.Map;


/**
 * Utilities for working with relative numbers in configurations.
 * <br>
 * Default data and user data are required for operation
 * <br><br>
 * Example:
 * <pre>
 * {@code
 * TsonMap defaults = new TsonMap("""
 *     {k1=(10), k2=(15), i1={k3=(16)}}
 * """);
 *
 * TsonMap src = new TsonMap("""
 *     {k1=(12), k2='+2', i1={k3='-7'}}
 * """);
 *
 * calcR(src, defaults);
 * //now src = "{k1=(12),k2=(17),i1={k3=(9)}}"
 * }
 * </pre>
 */
public class TsonNumUtils {
    /**
     * If a string is found in the user data that is located in the same place as the number in the default data,
     * then the following operations will be applied to this string:
     * <br><br>
     * 1. If the string is a number, then the string will be replaced with a number.
     * <br>
     * 2. If there is a mathematical operation sign in front of the received number,
     * then it is performed between the default value and the user value.
     * <br>
     * @param src User data
     * @param defaults Default data
     */
    public static void calcR(TsonMap src, TsonMap defaults){
        for (Map.Entry<String, TsonObj> obj: src.entrySet().toArray(new Map.Entry[0])){
            TsonObj def = defaults.get(obj.getKey());
            if(def == null)continue;
            TsonObj cur = obj.getValue();;

            if(cur.isMap() && def.isMap()){
                calcR(cur.getMap(), def.getMap());
            } else {
                try {
                    if (def.isNumber() && cur.isString()) {
                        Class<?> clazz = def.getClass();
                        if (clazz == TsonInt.class) {
                            src.fput(obj.getKey(), new TsonInt(calc(cur.getStr(), def.getInt())));
                        } else if (clazz == TsonDouble.class) {
                            src.fput(obj.getKey(), new TsonDouble(calc(cur.getStr(), def.getDouble())));
                        } else if (clazz == TsonFloat.class) {
                            src.fput(obj.getKey(), new TsonFloat(calc(cur.getStr(), def.getFloat())));
                        } else if (clazz == TsonLong.class) {
                            src.fput(obj.getKey(), new TsonLong(calc(cur.getStr(), def.getLong())));
                        }
                    } else if (def.isBool()) {
                        src.fput(obj.getKey(), new TsonBool(TsonBoolUtils.bool(cur.getStr(), def.getBool())));
                    }
                } catch (NumberFormatException e) {}
            }
        }
    }


    /**
     * See {@link #calcR(TsonMap, TsonMap)}}
     * @param src User data
     * @param key Key for access to TsonMap
     * @param def Default value
     * @return Mathematical int result between the default data and the user data by key
     */
    public static int calc(TsonMap src, String key, int def){
        return calc(src.get(key), def);
    }


    /**
     * See {@link #calc(TsonMap, String, int)}
     */
    public static int calc(TsonObj res, int def){
        if(res == null)return def;
        if(res.isNumber())return res.getInt();
        if(res.isString())return calc(res.getStr(), def);
        if(res.isBool())return res.getInt();
        throw new IllegalArgumentException();
    }


    /**
     * See {@link #calcR(TsonMap, TsonMap)}}
     * @param src User data
     * @param key Key for access to TsonMap
     * @param def Default value
     * @return Mathematical long result between the default data and the user data by key
     */
    public static long calc(TsonMap src, String key, long def){
        return calc(src.get(key), def);
    }


    /**
     * See {@link #calc(TsonMap, String, long)}
     */
    public static long calc(TsonObj res, long def){
        if(res == null)return def;
        if(res.isNumber())return res.getLong();
        if(res.isString())return calc(res.getStr(), def);
        throw new IllegalArgumentException();
    }


    /**
     * See {@link #calcR(TsonMap, TsonMap)}}
     * @param src User data
     * @param key Key for access to TsonMap
     * @param def Default value
     * @return Mathematical float result between the default data and the user data by key
     */
    public static float calc(TsonMap src, String key, float def){
        return calc(src.get(key), def);
    }


    /**
     * See {@link #calc(TsonMap, String, float)}
     */
    public static float calc(TsonObj res, float def){
        if(res == null)return def;
        if(res.isNumber())return res.getFloat();
        if(res.isString())return calc(res.getStr(), def);
        throw new IllegalArgumentException();
    }


    /**
     * See {@link #calcR(TsonMap, TsonMap)}}
     * @param src User data
     * @param key Key for access to TsonMap
     * @param def Default value
     * @return Mathematical double result between the default data and the user data by key
     */
    public static double calc(TsonMap src, String key, double def){
        return calc(src.get(key), def);
    }


    /**
     * See {@link #calc(TsonMap, String, double)}
     */
    public static double calc(TsonObj res, double def){
        if(res==null)return def;
        if(res.isNumber())return res.getFloat();
        if(res.isString())return calc(res.getStr(), def);
        throw new IllegalArgumentException();
    }


    /**
     * Defines and performs a mathematical operation between a string and int.
     * @param s Numbers with an operation
     * @param def Default value
     * @return Result of the operation
     */
    public static int calc(String s, int def) {
        switch (s.charAt(0)) {
            case '+': return def + Integer.parseInt(s.substring(1));
            case '-': return def - Integer.parseInt(s.substring(1));
            case '*': return (int) (def * Double.parseDouble(s.substring(1)));
            case '/': return (int) (def / Double.parseDouble(s.substring(1)));
            default : return Integer.parseInt(s);
        }
    }


    /**
     * Defines and performs a mathematical operation between a string and long.
     * @param s Numbers with an operation
     * @param def Default value
     * @return Result of the operation
     */
    public static long calc(String s, long def) {
        switch (s.charAt(0)) {
            case '+': return def + Long.parseLong(s.substring(1));
            case '-': return def - Long.parseLong(s.substring(1));
            case '*': return def * Long.parseLong(s.substring(1));
            case '/': return def / Long.parseLong(s.substring(1));
            default : return Long.parseLong(s);
        }
    }


    /**
     * Defines and performs a mathematical operation between a string and float.
     * @param s Numbers with an operation
     * @param def Default value
     * @return Result of the operation
     */
    public static float calc(String s, float def) {
        switch (s.charAt(0)) {
            case '+': return def + Float.parseFloat(s.substring(1));
            case '-': return def - Float.parseFloat(s.substring(1));
            case '*': return def * Float.parseFloat(s.substring(1));
            case '/': return def / Float.parseFloat(s.substring(1));
            default : return Float.parseFloat(s);
        }
    }


    /**
     * Defines and performs a mathematical operation between a string and double.
     * @param s Numbers with an operation
     * @param def Default value
     * @return Result of the operation
     */
    public static double calc(String s, double def) {
        switch (s.charAt(0)) {
            case '+': return def + Double.parseDouble(s.substring(1));
            case '-': return def - Double.parseDouble(s.substring(1));
            case '*': return def * Double.parseDouble(s.substring(1));
            case '/': return def / Double.parseDouble(s.substring(1));
            default : return Double.parseDouble(s);
        }
    }


    public static int[] intArr(TsonList list){
        int[] arr = new int[list.size()];
        for (int i = 0; i < arr.length; i++){
            arr[i] = calc(list.get(i), 0);
        }
        return arr;
    }


    public static double[] doubleArr(TsonList list){
        double[] arr = new double[list.size()];
        for (int i = 0; i < arr.length; i++){
            arr[i] = calc(list.get(i), 0);
        }
        return arr;
    }
}