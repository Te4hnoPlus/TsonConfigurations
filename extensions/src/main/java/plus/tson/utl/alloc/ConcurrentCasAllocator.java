package plus.tson.utl.alloc;

import java.util.function.Consumer;
import static plus.tson.utl.uns.UnsafeUtils.*;
import static plus.tson.utl.uns.UnsafeUtils21.compareAndSwap;


public class ConcurrentCasAllocator extends CasSegment implements Allocator{
    private static final long lastOffset = offset(ConcurrentCasAllocator.class, "last");
    CasSegment last = this;
    private final int maxLoop;
    private int loopSize;

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
        return last != null;
    }


    public final boolean containsRef(Object obj){
        if(obj == null)return true;
        CasSegment last;
        if((last = this.last) == null)return false;
        if(last.size > 0)
            for (Object check:last.data)if(check == obj)return true;
        CasSegment next = last.next;
        while (next != last){
            if(last.size > 0)
                for (Object check:last.data)if(check == obj)return true;
            next = next.next;
        }
        return false;
    }


    public final boolean containsObj(Object obj){
        if(obj == null)return true;
        CasSegment last;
        if((last = this.last) == null)return false;
        if(last.size > 0)
            for (Object check:last.data)
                if(check != null && check.equals(obj))return true;

        CasSegment next = last.next;
        while (next != last){
            if(last.size > 0)
                for (Object check:last.data)
                    if(check != null && check.equals(obj))return true;
            next = next.next;
        }
        return false;
    }


    public final void applyAll(Consumer<Object> consumer){
        CasSegment last;
        if((last = this.last) == null)return;
        if(last.size > 0)
            for (Object check:last.data)
                if(check != null)consumer.accept(check);

        CasSegment next = last.next;
        while (next != last){
            if(last.size > 0)
                for (Object check:last.data)
                    if(check != null)consumer.accept(check);
            next = next.next;
        }
    }


    public final int getLoopSize(){
        return loopSize;
    }


    public final int getMaxLoop(){
        return maxLoop;
    }


    @Override
    public final Object tryAllocRaw(){
        CasSegment last;
        if((last = this.last) == null)return null;
        Object result;
        if((result = last.tryAlloc0(this)) != null)return result;
        CasSegment next = last.prev;
        while (next != last){
            if((result = next.tryAlloc0(this)) != null)return result;
            next = next.prev;
        }
        compareAndSwap(this, lastOffset, last, null);
        return null;
    }


    @Override
    public final void freeObj(Object obj){
        CasSegment last;
        if((last = this.last) == null)last = this;
        if(last.freeObj0(this, obj))return;
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
            newArr.prev = next;
            this.prev = newArr;
        }
        if(!next.freeObj0(this, obj))freeObj(obj);
    }
}


class CasSegment{
    private static final long sizeOffset = offset(CasSegment.class, "size");
    CasSegment next = this, prev = this;
    final Object[] data;
    private volatile int cursor;
    volatile int size = 0;

    CasSegment(int size){
        this.data = new Object[size];
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


    private static void onTake(final CasSegment inst, final int size){
        if(!compareAndSwap(inst, sizeOffset, size, size-1))
            onTake(inst, inst.size);
    }


    final boolean freeObj0(ConcurrentCasAllocator parent, Object obj){
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


    private static void onInsert(final CasSegment inst, final int size){
        if(!compareAndSwap(inst, sizeOffset, size, size+1))
            onInsert(inst, inst.size);
    }
}