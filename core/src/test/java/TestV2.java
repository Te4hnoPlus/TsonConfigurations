import plus.tson.TsonMap;
import plus.tson.v2.STsonParser;

public class TestV2 {
    public static void main(String[] args) {
        STsonParser vv2 = new STsonParser("""
        {
        
        obj = new TestV2('test1', 'test2'){
            field1 = 'test3',
            field2 = this.test2('test4'),
            this.test3()
        }.name().length()

        }""");

        TsonMap res = vv2.compile();

        System.out.println(res.getField("obj"));
    }

    private String field1 = null;
    private String field2 = null;

    public TestV2(String s1, String s2){
        System.out.println(s1+":"+s2);
    }


    public void test1(String s){
        System.out.println("Call test:"+s);
    }


    public String test2(String s){
        return "__"+s+"__";
    }

    public void test3(){
        field1 = "X_"+ field1 +"_X";
        field2 = "X_"+ field2 +"_X";
    }

    public String name(){
        return "TestV2";
    }

    @Override
    public String toString() {
        return "TestV2{" +
                "val1='" + field1 + '\'' +
                ", val2='" + field2 + '\'' +
                '}';
    }
}
