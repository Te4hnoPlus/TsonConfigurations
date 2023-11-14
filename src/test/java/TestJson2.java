import plus.tson.*;

import java.io.File;

public class TestJson2 {
    public static void main(String[] args) {
        long l = System.currentTimeMillis();

        TsonFile.write(new File("test.txt"), "AGAGAG".getBytes());
//        TsonMap map = new TsonMap();
//        map.put("k1", "v1");
//        map.put("k2", "v2");
//        map.put("k3", l);
//
//        System.out.println(l);
//
//        System.out.println(map.toJsonStr());

//        System.out.println(new TsonMap("{k=true,v=false}"));
    }
}
