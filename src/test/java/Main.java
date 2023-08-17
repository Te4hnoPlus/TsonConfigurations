import plus.tson.*;
import plus.tson.exception.NoSearchException;
import plus.tson.security.ClassManager;

import java.io.File;


public class Main {
    public static void main(String[] args) {
        try{
            System.out.println();
            test2();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    
    public static void test2(){
        TsonMap mp = new TsonMap();
        TsonMap mpt = new TsonMap();
        mp.put("a","v");
        mp.put("f","d");
        mp.put("g","d");
        mpt.put("aaa",mp);


        System.out.println(mpt);


    }
    
    
    public static class Example implements TsonSerelizable{
        private final String k;
        private final String v;
        public Example(String k, String v){
            this.k = k;this.v = v;

        }

        public Example(TsonMap mp){
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
        for(int i=0;i<10_000_000;i++){
            String s = map.toString();
            map = new TsonMap(s);
        }
        System.out.println(
                10_000_000/((System.currentTimeMillis()-time)/1000f)
        );
    }
}