package plus.tson.utl.alloc;

import java.util.function.Consumer;
import static plus.tson.utl.uns.UnsafeUtils.*;


/**
 * Non-blocking Allocator implementation
 * <p>
 * Uses a combination of {@code Linked List}, Concurrent {@code Linked Queue}
 * and {@code Circular Queue} algorithms for the best performance and memory efficiency
 */
public class ConcurrentCasAllocator extends CasSegment implements Allocator{
    private static final long lastOffset = offset(ConcurrentCasAllocator.class, "last");
    //last used segment
    CasSegment last;
    private final int maxLoop;
    private int loopSize;

    /**
     * @param size Size of nodes in loop
     * @param maxLoop Max loop size
     */
    public ConcurrentCasAllocator(int size, int maxLoop){
        super(size);
        this.maxLoop = maxLoop;
    }


    /**
     * Template to allocator for objects
     * @param <T> type of object
     */
    public static abstract class Alloc<T> extends ConcurrentCasAllocator {
        /**
         * @param size Size of nodes in loop
         * @param maxLoop Max loop size
         */
        public Alloc(int size, int maxLoop) {
            super(size, maxLoop);
        }


        /**
         * @return Stored object or create new on fail
         */
        public T alloc(){
            Object result = tryAllocRaw();
            if(result != null)return (T) result;
            return create();
        }


        /**
         * Try to allocate object
         * @return allocated object or null on fail
         */
        public T tryAlloc(){
            return (T) tryAllocRaw();
        }


        /**
         * Put object to allocator, if allocator is not full
         * @param obj object to put
         */
        public void free(T obj){
            freeObj(obj);
        }


        /**
         * @return new T object instance, called on fail get stored object
         */
        public abstract T create();
    }


    /**
     * @return Probable sum of size of all segments
     */
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


    /**
     * Clear allocator
     */
    public final void clear(){
        clear(0);
    }


    /**
     * Reduce stored objects to {@code maxSize}
     */
    public final void clear(int maxSize){
        if(isEmpty())return;
        CasSegment next = this;
        Object[] data = next.data;
        Object prev;
        int size = this.data.length;

        if(next.size > 0) {
            for (int i = 0; i < size; i++) {
                if ((prev = data[i]) != null) {
                    if (maxSize > 0) --maxSize;
                    //compare and swap `data[i]`
                    else if (compareAndSwap(data, REF_SIZE_M2 + (REF_SIZE_D2 * i), prev, null)) {
                        onTake(next, next.size);
                    }
                }
            }
        }
        next = this.next;

        while (next != this){
            if(next.size > 0) {
                data = next.data;
                for (int i = 0; i < size; i++) {
                    if ((prev = data[i]) != null) {
                        if (maxSize > 0) --maxSize;
                        //compare and swap `data[i]`
                        else if (compareAndSwap(data, REF_SIZE_M2 + (REF_SIZE_D2 * i), prev, null)) {
                            onTake(next, next.size);
                        }
                    }
                }
            }
            next = next.next;
        }
    }


    /**
     * Empty always, then last is null
     */
    @Override
    public final boolean isEmpty(){
        return last != null;
    }


    /**
     * @param obj Object to check
     * @return true if object`s reference in allocator
     */
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


    /**
     * @param obj Object to check
     * @return true if object in allocator
     */
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


    /**
     * Accept function for all objects in allocator
     * @param consumer Function to accept
     */
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


    /**
     * @return current loop size
     */
    public final int getLoopSize(){
        return loopSize;
    }


    /**
     * @return max loop size, see {@link #ConcurrentCasAllocator(int, int)}
     */
    public final int getMaxLoop(){
        return maxLoop;
    }


    /**
     * Try to allocate object
     * @return allocated object or null on fail
     */
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
        //Atomic edit last used segment
        compareAndSwap(this, lastOffset, last, null);
        return null;
    }


    /**
     * Put object to allocator, if allocator is not full
     * @param obj object to put
     */
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
        //Don`t put object if allocator is full
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


/**
 * One memory segment of {@link ConcurrentCasAllocator}
 */
class CasSegment{
    private static final long sizeOffset = offset(CasSegment.class, "size");
    CasSegment next = this, prev = this;
    //data array
    final Object[] data;
    //last used cursor
    private volatile int cursor;
    //current segment size
    volatile int size = 0;

    CasSegment(int size){
        this.data = new Object[size];
    }


    /**
     * Try to allocate object from this segment
     * Use CAS to atomic access to data array
     * @return allocated object or null on fail
     */
    final Object tryAlloc0(ConcurrentCasAllocator parent){
        final int curSize;
        if((curSize = size) > 0) {
            final Object[] data;
            final int size = (data = this.data).length, cur = cursor;
            Object prev;
            for (int i = cur; i >= 0; i--) {
                if ((prev = data[i]) != null &&
                        //compare and swap `data[i]`
                        compareAndSwap(data, REF_SIZE_M2 + (REF_SIZE_D2 * i), prev, null)) {
                    if(i != 0)cursor = i-1;
                    else cursor = 0;
                    onTake(parent.last = this, curSize);
                    return prev;
                }
            }
            for (int i = cur; i < size; i++) {
                if ((prev = data[i]) != null &&
                        //compare and swap `data[i]`
                        compareAndSwap(data, REF_SIZE_M2 + (REF_SIZE_D2 * i), prev, null)) {
                    cursor = i;
                    onTake(parent.last = this, curSize);
                    return prev;
                }
            }
        }
        return null;
    }


    /**
     * Update segment size on take object
     */
    static void onTake(final CasSegment inst, final int size){
        if(!compareAndSwap(inst, sizeOffset, size, size-1))
            onTake(inst, inst.size);
    }


    /**
     * Try to put object to this segment
     * @param obj object to put
     * @return true if success
     */
    final boolean freeObj0(ConcurrentCasAllocator parent, Object obj){
        final Object[] data;
        final int size, curSize;
        if((size = (data = this.data).length) > (curSize = this.size)) {
            final int cur = cursor;
            for (int i = cur; i < size; i++) {
                if (data[i] == null &&
                        //compare and swap `data[i]`
                        compareAndSwap(data, REF_SIZE_M2+(REF_SIZE_D2 * i), null, obj)) {
                    this.cursor = i;
                    onInsert(parent.last = this, curSize);
                    return true;
                }
            }
            for (int i = cur; i >= 0; i--) {
                if (data[i] == null &&
                        //compare and swap `data[i]`
                        compareAndSwap(data, REF_SIZE_M2+(REF_SIZE_D2 * i), null, obj)) {
                    this.cursor = i;
                    onInsert(parent.last = this, curSize);
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Update segment size on insert object
     */
    static void onInsert(final CasSegment inst, final int size){
        if(!compareAndSwap(inst, sizeOffset, size, size+1))
            onInsert(inst, inst.size);
    }
}