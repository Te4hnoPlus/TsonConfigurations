import plus.tson.TsonList;
import plus.tson.TsonMap;

import static plus.tson.ext.TsonNumUtils.*;
import static plus.tson.ext.TsonAccessUtils.*;


public class Test {

    public static void main(String[] args) {
        testNums();
        testAccess();
    }


    private static void testNums(){
        TsonMap defaults = new TsonMap("""
                {k1=(10), k2=(15), i1={k3=(16)}}
                """);

        TsonMap src = new TsonMap("""
                {k1=(12), k2='+2', i1={k3='-7'}}
                """);

        calcR(src, defaults);

        //now src = "{k1=(12),k2=(17),i1={k3=(9)}}"

        System.out.println(src);
    }


    private static void testAccess(){
        TsonMap map = new TsonMap();
        TsonList list = map.addMap("test").addMap("test2").addList("test3");
        list.add(10);
        list.add(20);

        TsonMap map2 = new TsonMap();
        map2.put("kv1", 4);
        list.add(map2);

        System.out.println(getR(map, "test.test2.test3.2.kv10", "test.test2"));
    }
}
