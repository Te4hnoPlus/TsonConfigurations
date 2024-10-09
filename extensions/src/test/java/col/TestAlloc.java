package col;
import col.alloc.ConcurrentCASAllocatorLE;
import col.alloc.ConcurrentCASAllocator;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TestAlloc {
    private static AtomicInteger num1 = new AtomicInteger();
    private static AtomicInteger num2 = new AtomicInteger();
    private static AtomicInteger num3 = new AtomicInteger();

    public static final ConcurrentCASAllocatorLE.Alloc<int[]> alloc = new ConcurrentCASAllocatorLE.Alloc<>(
            20, 16) {
        //18
        @Override
        public int[] create() {
            return new int[]{num1.addAndGet(1)};
        }
    };

    public static final ConcurrentCASAllocator.Alloc<int[]> alloc2 = new ConcurrentCASAllocator.Alloc<>(
            20, 16) {
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

    public static void test1(){
        int size = 70;
        ArrayList<int[]> all = new ArrayList<>();
        int summ = 0;
        for (int i = 0; i < size; i++){
            all.add(alloc.alloc());
        }
        //System.out.println(alloc.size());
        for (int[] b:all){
            summ += b[0];
            alloc.free(b);
        }
        System.out.println("SIZE:"+alloc.size());

        int[] bytes = alloc.tryAlloc();
        int summ2 = 0;

        while (bytes != null){
            summ2 += bytes[0];
            bytes = alloc.tryAlloc();
        }

        System.out.println("S1: "+summ+", S2:"+summ2);
    }
    public static void test1V(){
        int size = 70;
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

        System.out.println("S1: "+summ+", S2:"+summ2);
    }


    public static void testSpeed(){
        int count = 50_0000;
        Random random = new Random();
        int[][] buf = new int[100][];
        for (int i = 0; i < 10; i++){
            random.setSeed(0);
            testTime(() -> {
                int[][] buf0 = buf;
                int rNum = random.nextInt(100);
                for (int i1 = 0; i1 < rNum; i1++){
                    buf0[i1] = jalloc.alloc();
                }
                rNum -= 4;
                for (int i1 = 0; i1 < rNum; i1++){
                    jalloc.free(buf0[i1]);
                }
            }, "J", count);

            random.setSeed(0);
            testTime(() -> {
                int[][] buf0 = buf;
                int rNum = random.nextInt(100);
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