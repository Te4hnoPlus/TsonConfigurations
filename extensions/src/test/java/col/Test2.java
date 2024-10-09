package col;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;

public class Test2 {
    public static void main(String[] args) {
        var bytes = UnsafeUtils.getBytes("Index out of range: ");
        bytes[0] = 'H'; bytes[1] = 'o'; bytes[2] = 'm'; bytes[3] = 'a'; bytes[4] = ' ';

        throw new IndexOutOfBoundsException(10);

//        String pooled1 = "pooled string";
//        String pooled2 = "pooled string";
//
//        System.out.println(pooled1);
//        UnsafeUtils.getBytes(pooled1)[6] = '_';
//
//        System.out.println(pooled2);
    }
}
