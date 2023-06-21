import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.TsonSerelizable;


public class Main {
    public static void main(String[] args) {
        try{
            test2();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    
    public static void test2(){
        TsonMap mp = new TsonMap();
        mp.put("k", (TsonSerelizable)new Ts2("av","ab"));

        System.out.println(mp);
        System.out.println(new TsonMap(mp.toString()));
    }
    
    
    public static class Ts2 implements TsonSerelizable{
        private final String k;
        private final String v;
        public Ts2(String k, String v){
            this.k = k;this.v = v;

        }
        
        public Ts2(TsonMap mp){
            k = mp.getStr("k");
            v = mp.getStr("v");
            System.out.println("NEN!");
        }
        
        @Override
        public TsonObj toTson() {
            TsonMap mp = new TsonMap();
            mp.put("k", k);
            mp.put("v",v);
            return mp;
        }
    }
    

    public static void test() {
        TsonMap map = new TsonMap();
        map.put("key1", "val1");
        map.put("key2", "val2");
        map.put("key3", "val3");
        map.put("key4", "val5");
        TsonList list = map.addList("items");
        list.add("item1");
        list.add("item2");
        list.add("item3");
        list.add("item4");
        list.add("item5");

        for (int i=0;i<7;i++){
            test0(map);
        }

    }


    public static void test0(TsonMap map){
        long time = System.currentTimeMillis();
        for(int i=0;i<1000_000;i++){
            String s = map.toString();
            map = new TsonMap(s);
        }
        System.out.println(
                1000_000/((System.currentTimeMillis()-time)/1000f)
        );
    }
}