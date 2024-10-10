package plus.tson.utl.alloc;

import java.util.concurrent.ConcurrentLinkedQueue;


public class ConcurrentLinkedAllocator extends ConcurrentLinkedQueue<Object> implements Allocator{
    @Override
    public Object tryAllocRaw() {
        return super.poll();
    }
    @Override
    public void freeObj(Object obj) {
        super.add(obj);
    }
}