import plus.tson.*;
import plus.tson.utl.TsonBinCoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestIO {
    public static void main(String[] args) throws IOException {
        TsonMap map = new TsonMap("{k1='v1', k2='v2', k3='v3'}");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TsonBinCoder.writeAll(out, map);

        System.out.println(map);

        InputStream in = new ByteArrayInputStream(out.toByteArray());

        TsonObj map2 = TsonBinCoder.readAll(in);

        System.out.println(map2);


    }
}
