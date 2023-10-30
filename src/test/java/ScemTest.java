import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.TsonSerelizable;
import plus.tson.utl.JSceme;

public class ScemTest {




    public static void main(String[] args) {
        JSceme<Test> schem = new JSceme<>(Test.class);

        Test test = schem.parse("{a1:2,a2:3,a3:'ss',map0:{t:'1',e:'fff'}}");

        System.out.println(test);
    }




    public static class Test{
        public int a1;
        public int a2;
        public String a3;
        public T2 map0;

        @Override
        public String toString() {
            TsonMap map = new TsonMap();
            map.put("a1", a1);
            map.put("a2", a2);
            map.put("a3", a3);
            map.put("map0", map0);
            return map.toJsonStr();
        }
    }


    public static class T2 implements TsonSerelizable {
        public String t;
        public String e;
        @Override
        public TsonObj toTson() {
            TsonMap map = new TsonMap();
            map.put("t", t);
            map.put("e", e);
            return map;
        }
    }
}