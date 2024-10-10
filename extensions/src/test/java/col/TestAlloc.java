package col;
import col.alloc.ConcurrentCASAllocatorLE;
import plus.tson.utl.alloc.ConcurrentCasAllocator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TestAlloc {
    private static AtomicInteger num1 = new AtomicInteger();
    private static AtomicInteger num2 = new AtomicInteger();
    private static AtomicInteger num3 = new AtomicInteger();

//    public static final ConcurrentCASAllocatorLE.Alloc<int[]> alloc = new ConcurrentCASAllocatorLE.Alloc<>(
//            64, 24) {
//        //18
//        @Override
//        public int[] create() {
//            return new int[]{num1.addAndGet(1)};
//        }
//    };

    public static final ConcurrentCasAllocator.Alloc<int[]> alloc2 = new ConcurrentCasAllocator.Alloc<>(
            32, 128) {
        //18
        @Override
        public int[] create() {
            return new int[]{num2.addAndGet(1)};
        }
    };
    public static final class JAlloc extends ConcurrentLinkedQueue<int[]> {
        public int[] alloc(){
            int[] result = super.poll();
            if(result == null)return new int[]{num3.addAndGet(1)};
            return result;
        }
        public void free(int[] obj){
            super.add(obj);
        }
    }
    public static final JAlloc jalloc = new JAlloc();


    public static void main(String[] args) {
        //test1();
        //test1V();
        testSpeed();
    }

    public static void test1V(){
        int size = 90;
        ArrayList<int[]> all = new ArrayList<>();
        int summ = 0;
        for (int i = 0; i < size; i++){
            all.add(alloc2.alloc());
        }
        //System.out.println(alloc2.size());
        for (int[] b:all){
            summ += b[0];
            alloc2.free(b);
        }
        System.out.println("SIZE:"+alloc2.size());

        int[] bytes = alloc2.tryAlloc();
        int summ2 = 0;

        while (bytes != null){
            summ2 += bytes[0];
            bytes = alloc2.tryAlloc();
        }

        System.out.println("S1: "+summ+", R2:"+summ2);
    }


    public static void testSpeed(){
        int count = 10_000;
        Random random = new Random();
        int bufSize = 1000;
        int[][] buf = new int[bufSize*2][];
        for (int i = 0; i < 10; i++){
//            random.setSeed(0);
//            testTime(() -> {
//                int[][] buf0 = buf;
//                int rNum = random.nextInt(bufSize)+bufSize;
//                for (int i1 = 0; i1 < rNum; i1++){
//                    buf0[i1] = jalloc.alloc();
//                }
//                rNum -= 4;
//                for (int i1 = 0; i1 < rNum; i1++){
//                    jalloc.free(buf0[i1]);
//                }
//            }, "J", count);

            random.setSeed(0);
            testTime(() -> {
                int[][] buf0 = buf;
                int rNum = random.nextInt(bufSize)+bufSize;
                for (int i1 = 0; i1 < rNum; i1++){
                    buf0[i1] = alloc2.alloc();
                }
                rNum -= 4;
                for (int i1 = 0; i1 < rNum; i1++){
                    alloc2.free(buf0[i1]);
                }
            }, "V2", count);
        }
    }


    public static void testTime(Runnable r, String name, int count){
        long time = System.currentTimeMillis();
        for (int i = 0; i < count; i++){
            r.run();
        }
        time = System.currentTimeMillis() - time;
        System.out.println(name + " : " + time+"ms");
    }
}