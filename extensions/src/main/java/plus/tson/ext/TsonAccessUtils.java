package plus.tson.ext;

import plus.tson.TsonList;
import plus.tson.TsonObj;


/**
 * Utilities to simplify access to the Tson data hierarchy
 */
public class TsonAccessUtils {
    /**
     * @param src TsonMap in which to search for data
     * @param path A sequential list of keys separated by dot in depth for TsonMap or TsonList
     * @return Value if the path was correct or default
     */
    public static TsonObj getR(TsonObj src, String path, TsonObj def){
        int end = path.indexOf('.');
        if(end == -1){
            if(src.isMap())return src.getMap().get(path);
            if(src.isList())return getByList(src.getList(), path);
            return null;
        }
        int start = 0;

        do {
            TsonObj obj = null;
            if(src.isMap())obj = src.getMap().get(path.substring(start, end));
            if(src.isList())obj = getByList(src.getList(), path.substring(start, end));
            if (obj == null)return def;

            if((end = path.indexOf('.', start = end+1)) == -1){
                if(obj.isMap())return obj.getMap().get(path.substring(start));
                if(obj.isList())return getByList(obj.getList(), path.substring(start));
                return def;
            }
            if(obj.isMap() || obj.isList())src = obj;
            else src = null;
        } while (src != null);
        return def;
    }


    /**
     * See {@link #getR(TsonObj, String, TsonObj)}
     * @return Value if the path was correct or null
     */
    public static TsonObj getR(TsonObj src, String path){
        return getR(src, path, null);
    }


    /**
     * See {@link #getR(TsonObj, String, TsonObj)}
     * @param src TsonMap in which to search for data
     * @param keys Paths separated by dot
     * @param def Default result
     * @return First found result from the list of paths or default
     */
    public static TsonObj getR(TsonObj src, TsonObj def, String... keys){
        for (String key:keys){
            TsonObj res = getR(src, key);
            if(res != null)return res;
        }
        return def;
    }


    /**
     * See {@link #getR(TsonObj, TsonObj, String...)}
     * @return First found result from the list of paths or null
     */
    public static TsonObj getR(TsonObj src, String... keys){
        return getR(src, null, keys);
    }


    private static TsonObj getByList(TsonList temp, String indexRaw){
        int index;
        try {
            index = Integer.parseInt(indexRaw);
        } catch (NumberFormatException e){
            return null;
        }
        if(0 > index)index = temp.size() - index;
        if(0 > index || index >= temp.size())return null;
        return temp.get(index);
    }


    /**
     * See {@link #getR(TsonObj, String, TsonObj)}
     * @return Value if the path was correct or default
     */
    public static boolean getR(TsonObj src, String key, boolean def){
        return TsonBoolUtils.calc(getR(src, key, null), def);
    }


    /**
     * See {@link #getR(TsonObj, String, TsonObj)}
     * @return Value if the path was correct or default
     */
    public static int getR(TsonObj src, String key, int def){
        return TsonNumUtils.calc(getR(src, key, null), def);
    }


    /**
     * See {@link #getR(TsonObj, String, TsonObj)}
     * @return Value if the path was correct or default
     */
    public static long getR(TsonObj src, String key, long def){
        return TsonNumUtils.calc(getR(src, key, null), def);
    }


    /**
     * See {@link #getR(TsonObj, String, TsonObj)}
     * @return Value if the path was correct or default
     */
    public static float getR(TsonObj src, String key, float def){
        return TsonNumUtils.calc(getR(src, key, null), def);
    }


    /**
     * See {@link #getR(TsonObj, String, TsonObj)}
     * @return Value if the path was correct or default
     */
    public static double getR(TsonObj src, String key, double def){
        return TsonNumUtils.calc(getR(src, key, null), def);
    }


    /**
     * See {@link #getR(TsonObj, TsonObj, String...)}
     * @return First found result from the list of paths or default
     */
    public static boolean getR(TsonObj src, boolean def, String... keys){
        return TsonBoolUtils.calc(getR(src, null, keys), def);
    }


    /**
     * See {@link #getR(TsonObj, TsonObj, String...)}
     * @return First found result from the list of paths or default
     */
    public static int getR(TsonObj src, int def, String... keys){
        return TsonNumUtils.calc(getR(src, null, keys), def);
    }


    /**
     * See {@link #getR(TsonObj, TsonObj, String...)}
     * @return First found result from the list of paths or default
     */
    public static long getR(TsonObj src, long def, String... keys){
        return TsonNumUtils.calc(getR(src, null, keys), def);
    }


    /**
     * See {@link #getR(TsonObj, TsonObj, String...)}
     * @return First found result from the list of paths or default
     */
    public static float getR(TsonObj src, float def, String... keys){
        return TsonNumUtils.calc(getR(src, null, keys), def);
    }


    /**
     * See {@link #getR(TsonObj, TsonObj, String...)}
     * @return First found result from the list of paths or default
     */
    public static double getR(TsonObj src, double def, String... keys){
        return TsonNumUtils.calc(getR(src, null, keys), def);
    }
}