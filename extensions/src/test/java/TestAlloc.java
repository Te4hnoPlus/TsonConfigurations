import plus.tson.utl.Allocator;
import plus.tson.utl.CharStrBuilder;

public class TestAlloc {
    public static void main(String[] args) {
        CharStrBuilder builder = new CharStrBuilder(16);

//        builder.append(',');
//
//        builder.append("test");
//        builder.append(',');
//        builder.append("_test2");
//        builder.append(',');
        //builder.append(-123);

        System.out.println(builder.toString());


//        Allocator<String> alc = new Allocator<String>() {
//            private int num = 0;
//            @Override
//            protected String newObj() {
//                return "TEST "+ ++num;
//            }
//        };
//
//        String s = alc.malloc();
//        String s2 = alc.malloc();
//
//        alc.free(s);
//        alc.free(s2);
//
//
//        System.out.println(alc);
    }
}
