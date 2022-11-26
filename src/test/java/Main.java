import plus.tson.TsonFile;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonSerelizable;


public class Main {
    public static void main(String[] args) {
        try{
            test();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void test() {
        TsonFile map = new TsonFile("data.tson");
        TsonList list = map.addList("test_v");
        list.add(new TestV(1, "mam"));
        list.add(new TestV(2, "bum"));
        list.add(new TestV(3, "dem"));
        map.save();

        System.out.println(map);
    }

    protected static class TestV implements TsonSerelizable {
        public final int A;
        public final String T;

        public TestV(int a, String t) {
            this.A = a;
            this.T = t;
        }


        public TestV(TsonMap map) {
            A = map.getInt("A");
            T = map.getStr("T");
        }


        @Override
        public TsonMap toMap() {
            TsonMap map = new TsonMap();
            map.put("A", A);
            map.put("T", T);
            return map;
        }
    }
}