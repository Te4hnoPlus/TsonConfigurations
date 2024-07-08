import plus.tson.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Main {
    public static void main(String[] args) {
        try{
            //while (true)
            test2();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static HashMap<String,ArrayList<Double>> result = new HashMap<>();
    
    public static void test2(){
        String data = "{ak1: true,bk1: false,ck1: 20,dk1: 50,ek1: 'test',dk1: 'gaga',ek1: 'gaga4'}";
        TsonMap map = new TJsonParser(data,true).getMap();
        int count = 1000_000;

        for (int i=1;i<11;i++){
            testTime(() ->   testSpeedJS(map, count), "js", count);
            testTime(() -> testSpeedJSV2(map, count), "v2", count);
            //testTime(() ->   testSpeedTS(map, count), "ts", count);
        }
        resToStr();
    }

    private static void resToStr(){
        String name = System.getProperty("java.vm.name")+":"+System.getProperty("java.version");
        String prefix = "\n----------------------------------------------\n";
        String body = name+prefix;
        for (Map.Entry<String,ArrayList<Double>> ls:result.entrySet()){
            double max = 0;
            int count = 0;
            double summ = 0;
            for (Double d:ls.getValue()){
                count+=1;
                summ+=d;
                if(d>max){
                    max = d;
                }
            }
            body = body + (ls.getKey()+": "+ (((int)(summ/count*10))/10f) +"\n");
            body = body + "max-"+(ls.getKey()+": "+ (((int)(max*10))/10f) +"\n");
        }
        String nm2 = remBad(System.getProperty("java.vm.name"))
                +"-"+remBad(System.getProperty("java.version"))+".txt";
        File file = new File(nm2);
        TsonFile.write(file, body);
    }


    private static String remBad(String s){
        String name = s.trim().replace(".", "-").replace("(", "")
                .replace(")", "").replace(" ", "");
        if(name.length()>8)return name.substring(0, 8);
        return name;
    }


    static void testTime(Runnable r, String label, int count){
        long start = System.currentTimeMillis();
        r.run();
        double ops = count*1000f/(System.currentTimeMillis()-start);
        ArrayList<Double> anl = result.get(label);
        if(anl==null){
            result.put(label, anl=new ArrayList<>());
        }
        anl.add(ops);
        System.out.println("["+label+"] OPS/PS: "+ ops);
    }


    static void testSpeedTS(TsonMap map, int count){
        String code = map.toString();
        for(int i=0;i<count;i++){
            code = new TsonMap(code).toString();
        }
    }


    static void testSpeedJS(TsonMap map, int count){
        String code = map.toJsonObj();
        for(int i=0;i<count;i++){
            code = new TJsonParser(code, true).getMap().toJsonObj();
        }
    }


    static void testSpeedJSV2(TsonMap map, int count){
        String code = map.toJsonObj();
        for(int i=0;i<count;i++){
            code = new TJsonParserV2(code, true).getMap().toJsonObj();
        }
    }
}