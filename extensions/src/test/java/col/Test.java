package col;

import plus.tson.utl.uns.UnsafeUtils;

public class Test {

    public static void main(String[] args) {
        //ArrayDeque
        //ConcurrentLinkedQueue

        byte[] bytes = "12345".getBytes();

        String mutStr = UnsafeUtils.stringOf(bytes);

        System.out.println(mutStr);

        bytes[0] = 'X';

        System.out.println(mutStr);

        System.out.println(bytes == UnsafeUtils.getBytes(mutStr));
    }
}
