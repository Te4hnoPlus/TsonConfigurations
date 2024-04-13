//import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
//import plus.tson.utl.Te4HashSet;
//
//import java.lang.reflect.Field;
//import java.util.IdentityHashMap;

public class ScanMem {
//    private static final Int2IntOpenHashMap table = new Int2IntOpenHashMap();
//    private static final int refSize = 4;
//    static {
//        table.put(byte.class.hashCode(),      1);
//        table.put(short.class.hashCode(),     2);
//        table.put(int.class.hashCode(),       4);
//        table.put(long.class.hashCode(),      8);
//        table.put(float.class.hashCode(),     4);
//        table.put(double.class.hashCode(),    8);
//        table.put(char.class.hashCode(),      2);
//        table.put(boolean.class.hashCode(),   1);
//        table.defaultReturnValue(0);
//    }
//
//    private static final IdentityHashMap<Object,Object> cache = new IdentityHashMap<>();
//    private static final Te4HashSet<String> noCheck = new Te4HashSet<>();
//
//    public static void scanMem(Object obj){
//        if(cache.containsKey(obj))return;
//        cache.put(obj, obj);
//
//        Field[] fields = obj.getClass().getDeclaredFields();
//
//        for (Field field:fields){
//            System.out.println(field.getGenericType());
//            System.out.println(field.getModifiers());
//        }
//    }
//
//
//    public static void main(String[] args) {
//        scanMem(new ScanMem());
//    }
}
