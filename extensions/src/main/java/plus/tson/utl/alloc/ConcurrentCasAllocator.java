package plus.tson.utl.alloc;

import static plus.tson.utl.uns.UnsafeUtils.*;
import static plus.tson.utl.uns.UnsafeUtils21.compareAndSwap;


public class ConcurrentCasAllocator extends CasSegment implements Allocator{
    private final int maxLoop;
    private volatile int loopSize;

    public ConcurrentCasAllocator(int maxNodes, int maxLoop){
        super(maxNodes);
        this.maxLoop = maxLoop;
    }


    public static abstract class Alloc<T> extends ConcurrentCasAllocator {
        public Alloc(int countNodes, int maxLoop) {
            super(countNodes, maxLoop);
        }


        public T alloc(){
            Object result = tryAllocRaw();
            if(result != null)return (T) result;
            return create();
        }


        public T tryAlloc(){
            return (T) tryAllocRaw();
        }


        public void free(T obj){
            freeObj(obj);
        }


        public abstract T create();
    }


    @Override
    public final int size(){
        int curSize = this.size;
        CasSegment next = this.next;
        while (next != this){
            curSize += next.size;
            next = next.next;
        }
        return curSize;
    }


    @Override
    public final boolean isEmpty(){
        if(this.size != 0)return false;
        CasSegment next = this.next;
        while (next != this){
            if(next.size != 0)return false;
            next = next.next;
        }
        return true;
    }


    public final int loopSize(){
        return loopSize;
    }


    @Override
    public final Object tryAllocRaw(){
        Object result;
        CasSegment last;
        if((result = (last = this.last).tryAlloc0(this)) != null)return result;
        CasSegment next = this.next;
        while (next != last){
            if((result = next.tryAlloc0(this)) != null)return result;
            next = next.next;
        }
        return null;
    }


    @Override
    public final void freeObj(Object obj){
        final CasSegment last;
        if((last = this.last).freeObj0(this, obj))return;
        CasSegment next = last.next;
        while (next != last){
            if(next.freeObj0(this, obj))return;
            next = next.next;
        }
        int maxLoop;
        if(loopSize >= (maxLoop = this.maxLoop))return;
        synchronized (this){
            if(loopSize >= maxLoop)return;
            ++loopSize;
            CasSegment newArr = new CasSegment(this.data.length);
            newArr.next = next.next;
            next.next = newArr;
        }
        if(!next.freeObj0(this, obj))freeObj(obj);
    }
}


class CasSegment{
    private static final long sizeOffset = offset(CasSegment.class, "size");
    CasSegment next = this, last = this;
    final Object[] data;
    private volatile int cursor;
    volatile int size = 0;

    CasSegment(int size){
        this.data = new Object[size];
    }


    private static void onInsert(final CasSegment inst, final int size){
        if(!compareAndSwap(inst, sizeOffset, size, size+1))
            onInsert(inst, inst.size);
    }
    private static void onTake(final CasSegment inst, final int size){
        if(!compareAndSwap(inst, sizeOffset, size, size-1))
            onTake(inst, inst.size);
    }


    final Object tryAlloc0(ConcurrentCasAllocator parent){
        final int curSize;
        if((curSize = size) > 0) {
            final Object[] data;
            final int size = (data = this.data).length, cur = cursor;
            Object prev;
            for (int i = cur; i >= 0; i--) {
                if ((prev = data[i]) != null &&
                        compareAndSwap(data, REF_SIZE_M2 + (REF_SIZE_D2 * i), prev, null)) {
                    if(i != 0)cursor = i-1;
                    else cursor = 0;
                    onTake(parent.last = this, curSize);
                    return prev;
                }
            }
            for (int i = cur; i < size; i++) {
                if ((prev = data[i]) != null &&
                        compareAndSwap(data, REF_SIZE_M2 + (REF_SIZE_D2 * i), prev, null)) {
                    cursor = i;
                    onTake(parent.last = this, curSize);
                    return prev;
                }
            }
        }
        return null;
    }


    final boolean freeObj0(CasSegment parent, Object obj){
        final Object[] data;
        final int size, curSize;
        if((size = (data = this.data).length) > (curSize = this.size)) {
            final int cur = cursor;
            for (int i = cur; i < size; i++) {
                if (data[i] == null &&
                        compareAndSwap(data, REF_SIZE_M2+(REF_SIZE_D2 * i), null, obj)) {
                    this.cursor = i;
                    onInsert(parent.last = this, curSize);
                    return true;
                }
            }
            for (int i = cur; i >= 0; i--) {
                if (data[i] == null &&
                        compareAndSwap(data, REF_SIZE_M2+(REF_SIZE_D2 * i), null, obj)) {
                    this.cursor = i;
                    onInsert(parent.last = this, curSize);
                    return true;
                }
            }
        }
        return false;
    }
}