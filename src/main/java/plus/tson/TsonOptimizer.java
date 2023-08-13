package plus.tson;

import plus.tson.exception.NoSearchException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class TsonOptimizer {
    private static boolean tryUseNetty = true;
    private final Map<Integer, ArrayList<Object>> cache;

    public TsonOptimizer(){
        Map<Integer, ArrayList<Object>> preCache;
        if(tryUseNetty) {
            try {
                preCache = (Map<Integer, ArrayList<Object>>)
                        new TsonClass("io.netty.util.collection.IntObjectHashMap").createInst();
            } catch (NoSearchException e) {
                preCache = new HashMap<>();
                tryUseNetty = false;
            }
        } else {
            preCache = new HashMap<>();
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
        TsonMap result = new TsonMap();
        for(Map.Entry<String, TsonObj> entry:src.entrySet()){
            TsonObj val = entry.getValue();
            if(val.isList()){
                result.put(
                        (String) getOptimalObj(entry.getKey()), optimize(val.getList())
                );
            } else if(val.isMap()){
                result.put(
                        (String) getOptimalObj(entry.getKey()), optimize(val.getMap())
                );
            } else {
                result.put((String) getOptimalObj(entry.getKey()), (TsonObj) getOptimalObj(val));
            }
        }
        return result;
    }
}