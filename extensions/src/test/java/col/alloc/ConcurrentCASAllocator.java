package col.alloc;

import static col.UnsafeUtils.*;
//import static col.Unsafe21.compareAndSwap;


public class ConcurrentCASAllocator {
    private static final long sizeOffset = offset(ConcurrentCASAllocator.class, "size");
    ConcurrentCASAllocator next = this, last = this;
    private final Object[] data;
    private volatile int cursor;
    private volatile int size = 0;
    private final int maxLoop;

    public ConcurrentCASAllocator(int maxNodes, int maxLoop){
        this.data = new Object[maxNodes];
        this.maxLoop = maxLoop;
    }

    ConcurrentCASAllocator(ConcurrentCASAllocator parent){
        this.data = new Object[parent.data.length];
        this.maxLoop = parent.maxLoop;
    }


    public static abstract class Alloc<T> extends ConcurrentCASAllocator {
        public Alloc(int countNodes, int maxLoop) {
            super(countNodes, maxLoop);
        }


        public T alloc(){
            Object result = last.tryAllocRaw();
            if(result != null)return (T) result;
            return create();
        }


        public T tryAlloc(){
            return (T) last.tryAllocRaw();
        }


        public void free(T obj){
            freeObj(obj);
        }


        public abstract T create();
    }


    public final int size(){
        int curSize = this.size;
        ConcurrentCASAllocator next = this.next;
        while (next != this){
            curSize += next.size;
            next = next.next;
        }
        return curSize;
    }


    public final boolean isEmpty(){
        if(this.size != 0)return false;
        ConcurrentCASAllocator next = this.next;
        while (next != this){
            if(next.size != 0)return false;
            next = next.next;
        }
        return true;
    }


    public final int loopSize(){
        int curSize = 1;
        ConcurrentCASAllocator next = this.next;
        while (next != this){
            ++curSize;
            next = next.next;
        }
        return curSize;
    }


    private static void onInsert(final ConcurrentCASAllocator inst, final int size){
        if(!compareAndSwap(inst, sizeOffset, size, size+1))
            onInsert(inst, inst.size);
    }
    private static void onTake(final ConcurrentCASAllocator inst, final int size){
        if(!compareAndSwap(inst, sizeOffset, size, size-1))
            onTake(inst, inst.size);
    }


    public final Object tryAllocRaw(){
        Object result;
        if((result = tryAlloc0(this)) != null)return result;
        ConcurrentCASAllocator next = this.next;
        while (next != this){
            if((result = next.tryAlloc0(this)) != null)return result;
            next = next.next;
        }
        return null;
    }


    private Object tryAlloc0(ConcurrentCASAllocator parent){
        final int curSize;
        if((curSize = size) > 0) {
            final Object[] data;
            final int size = (data = this.data).length, cur = cursor;
            Object prev;
            for (int i = cur; i < size; i++) {
                if ((prev = data[i]) != null &&
                        compareAndSwap(data, REF_SIZE_M2 + (REF_SIZE_D2 * i), prev, null)) {
                    cursor = i + 1;
                    onTake(parent.last = this, curSize);
                    return prev;
                }
            }
            for (int i = 0; i < cur; i++) {
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


    public final void freeObj(Object obj){
        final ConcurrentCASAllocator last;
        if((last = this.last).freeObj0(this, obj))return;
        ConcurrentCASAllocator next = last.next;
        while (next != last){
            if(next.freeObj0(this, obj))return;
            next = next.next;
        }
        if(loopSize() >= maxLoop)return;
        synchronized (this){
            ConcurrentCASAllocator newArr = new ConcurrentCASAllocator(this);
            newArr.next = next.next;
            next.next = newArr;
        }
        if(!next.freeObj0(this, obj))freeObj(obj);
    }


    private boolean freeObj0(ConcurrentCASAllocator parent, Object obj){
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
            for (int i = 0; i < cur; i++) {
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