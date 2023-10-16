import plus.tson.*;
import java.nio.charset.StandardCharsets;


public class Main {
    public static void main(String[] args) {
        try{
            test2();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    
    public static void test2(){
        String data = "{a:true,b:false,c:20,d:50,e:'test',d:'gaga'}";
        TsonMap map = new TJsonParser(data.getBytes(StandardCharsets.UTF_8)).getMap();
        int count = 2000_000;

        for (int i=1;i<11;i++){
            System.out.println("TEST "+i);
            testTime(() -> testSpeedJS(map, count), count);
            testTime(() -> testSpeedTS(map, count), count);
        }
    }



    static void testTime(Runnable r, int count){
        long start = System.currentTimeMillis();
        r.run();
        System.out.println("OPS/PS: "+ count*1000f/(System.currentTimeMillis()-start));
    }


    static void testSpeedTS(TsonMap map, int count){
        String code = map.toString();
        for(int i=0;i<count;i++){
            code = new TsonMap(code).toString();
        }
    }


    static void testSpeedJS(TsonMap map, int count){
        String code = map.toJsonStr();
        for(int i=0;i<count;i++){
            code = new TJsonParser(code.getBytes()).getMap().toJsonStr();
        }
    }
}