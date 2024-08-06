package plus.tson;

import plus.tson.utl.Te4HashMap;
import java.util.ArrayList;
import java.util.Map;


/**
 * It is designed to optimize memory in Json objects.
 * <br>
 * All pointers that repeatedly point to an equivalent object will now point to the main object
 */
public class TsonOptimizer {
    private static boolean tryUseNetty = true;
    private final Map<Integer, ArrayList<Object>> cache;

    public TsonOptimizer(){
        Map<Integer, ArrayList<Object>> preCache;
        if(tryUseNetty) {
            //Attempt to use Netty quick collection if this framework has been detected
            try {
                preCache = (Map<Integer, ArrayList<Object>>)
                        new TsonClass("io.netty.util.collection.IntObjectHashMap").createInst();
            } catch (IllegalArgumentException e) {
                preCache = new Te4HashMap<>();
                tryUseNetty = false;
            }
        } else {
            preCache = new Te4HashMap<>();
        }
        cache = preCache;
    }


    public void clearCache(){
        cache.clear();
    }


    private ArrayList<Object> getArr(int num){
        return cache.computeIfAbsent(num, k -> new ArrayList<>());
    }


    public Object getOptimalObj(Object obj){
        ArrayList<Object> arr = getArr(obj.hashCode());
        for(Object check:arr){
            if(check.equals(obj))return check;
        }
        arr.add(obj);
        return obj;
    }


    public void feed(Object... objects){
        for(Object obj:objects) {
            ArrayList<Object> arr = getArr(obj.hashCode());
            for (Object check : arr) {
                if (check.equals(obj)) return;
            }
            arr.add(obj);
        }
    }


    public TsonList optimize(TsonList src){
        int size = src.size();
        TsonList list = new TsonList(size);
        for (TsonObj val : src) {
            if (val.isList()) {
                list.add(optimize(val.getList()));
            } else if (val.isMap()) {
                list.add(optimize(val.getMap()));
            } else {
                list.add((TsonObj) getOptimalObj(val));
            }
        }
        return list;
    }


    public TsonMap optimize(TsonMap src){
        TsonMap map = new TsonMap();
        optimize(src, map);
        return map;
    }


    public TsonFile optimize(TsonFile src){
        TsonFile file = new TsonFile(src.getFile());
        optimize(src, file);
        return file;
    }


    public void optimize(TsonMap from, TsonMap to){
        if(from==to)throw new IllegalArgumentException();
        for(Map.Entry<String, TsonObj> entry:from.entrySet()){
            TsonObj val = entry.getValue();
            if(val.isList()){
                to.put(
                        (String) getOptimalObj(entry.getKey()), optimize(val.getList())
                );
            } else if(val.isMap()){
                to.put(
                        (String) getOptimalObj(entry.getKey()), optimize(val.getMap())
                );
            } else {
                to.put((String) getOptimalObj(entry.getKey()), (TsonObj) getOptimalObj(val));
            }
        }
    }
}