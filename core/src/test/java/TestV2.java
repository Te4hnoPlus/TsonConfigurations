import plus.tson.TsonMap;
import plus.tson.STsonParser;

public class TestV2 {
    public static void main(String[] args) {
        int length = new STsonParser("{\n" +
                "\n" +
                "        obj = new TestV2('test1', 'test2', {'key test' = true}){\n" +
                "            field1 = 'test3',\n" +
                "            field2 = this.test2('test4'),\n" +
                "            field3 = 'abc'.length(),\n" +
                "            this.test3()\n" +
                "        }.name().length()\n" +
                "\n" +
                "        }"
        ).getMap().getInt("obj");

        System.out.println(length);
    }

    private String field1 = null;
    private String field2 = null;
    private int field3;

    public TestV2(String s1, String s2, TsonMap map){
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
