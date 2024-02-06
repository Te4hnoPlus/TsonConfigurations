import plus.tson.TsonMap;

public class TestA {


    public static void main(String[] args) {

        new TsonMap(
                """
                        {k = <(TestA$Test), <(TestA$Test2)>, "b"> }
                        """
        );

        System.out.println("NEN");
    }

    static class Test2{
        public Test2(){
            System.out.println("A!");
        }
    }

    static class Test{
        public Test(Test2 s, String s2){
            System.out.println(s+"-"+s2);
        }
    }
}
