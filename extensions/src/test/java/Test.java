import plus.tson.*;

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
        TsonMap src2 = new TsonMap("""
                {key1=(true), key2=(false), data={key3=(3), key4=(4)}, list=[(2), (3), (4)]}
                """);

        calcR(src, defaults);

        //now src = "{k1=(12),k2=(17),i1={k3=(9)}}"

        System.out.println(src);
    }


    static TestItem search(int id, TestItem... list){
        for(TestItem item: list){
            if(item.getId() == id) return item;
        }
        return null;
    }


    public static class TestItem implements TsonSerelizable {
        private final String name;
        private final int id;
        private final String description;

        public TestItem(TsonObj obj){
            name        = getR("EMPTY", obj, "name");
            id          = getR(obj, "id", 0);
            description = getR("EMPTY", obj, "description");
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public String getName() {
            return name;
        }

        public int compareTo(TestItem o) {
            int cmp = Integer.compare(id, o.id);
            if(cmp != 0) return cmp;
            cmp = name.compareTo(o.name);
            if(cmp != 0) return cmp;
            return description.compareTo(o.description);
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof TestItem)) return false;
            return compareTo((TestItem) obj) == 0;
        }

        @Override
        public TsonObj toTson() {
            TsonMap map = new TsonMap();
            map.put("name", name);
            map.put("id", id);
            map.put("description", description);
            return map;
        }
    }




    private static void testAccess(){
        TsonMap map = new TsonMap();
        TsonList list = map.addMap("test").addMap("test2").addList("test3");
        list.add(10);
        list.add(20);

        TsonMap map2 = new TsonMap();
        map2.put("kv1", 4);
        list.add(map2);

        System.out.println(getR("DEF", map, "test.test2.test3.2.kv10", "test.test2"));
        System.out.println(getR("DEF", map, "test.test2.test3.2.kv10", "test.test2"));
    }
}
