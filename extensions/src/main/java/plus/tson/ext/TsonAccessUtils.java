package plus.tson.ext;

import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;


/**
 * Utilities to simplify access to the Tson data hierarchy
 */
public class TsonAccessUtils {
    /**
     * @param src TsonMap in which to search for data
     * @param path A sequential list of keys separated by dot in depth for TsonMap or TsonList
     * @return Value if the path was correct, or null
     */
    public static TsonObj getR(TsonObj src, String path){
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
            if (obj == null)return null;

            if((end = path.indexOf('.', start = end+1)) == -1){
                if(obj.isMap())return obj.getMap().get(path.substring(start));
                if(obj.isList())return getByList(obj.getList(), path.substring(start));
                return null;
            }
            if(obj.isMap() || obj.isList())src = obj;
            else src = null;
        } while (src != null);
        return null;
    }


    /**
     * See {@link #getR(TsonObj, String)}
     * @param src TsonMap in which to search for data
     * @param keys Paths separated by dot
     * @return First found result from the list of paths
     */
    public static TsonObj getR(TsonMap src, String... keys){
        for (String key:keys){
            TsonObj res = getR(src, key);
            if(res != null)return res;
        }
        return null;
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
}