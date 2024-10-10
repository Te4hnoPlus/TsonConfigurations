package col.alloc;

import static plus.tson.utl.uns.UnsafeUtils.UNSAFE;
import static plus.tson.utl.uns.UnsafeUtils.*;


public class ConcurrentCASAllocatorLE {
    private static final long sizeOffset = offset(ConcurrentCASAllocatorLE.class, "size");
    ConcurrentCASAllocatorLE parent = this;
    ConcurrentCASAllocatorLE last = this;
    ConcurrentCASAllocatorLE next = this;
    private final int maxLoop;
    private final Node[] nodes;
    private volatile int cursor;
    private volatile int size = 0;

    protected ConcurrentCASAllocatorLE(int countNodes, int maxLoop) {
        Node[] nodes = this.nodes = new Node[countNodes];
        for (int i = 0; i < countNodes; i++){
            nodes[i] = new Node();
        }
        this.maxLoop = maxLoop;
    }


    public static abstract class Alloc<T> extends ConcurrentCASAllocatorLE {
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


    private Object allocObj0(){
        if(size > 0) {
            Node[] nodes = this.nodes;
            int size = nodes.length;
            int cur = cursor;
            Object result;
            for (int i = cur; i < size; i++) {
                if ((result = nodes[i].tryGet()) != null) {
                    this.cursor = i+1;
                    this.onTake();
                    return result;
                }
            }
            for (int i = 0; i < cur; i++) {
                if ((result = nodes[i].tryGet()) != null) {
                    this.cursor = i;
                    this.onTake();
                    return result;
                }
            }
        }
        return null;
    }


    public final Object tryAllocRaw(){
        Object result;
        if((result = allocObj0()) != null)return result;
        ConcurrentCASAllocatorLE next = this.next;
        while (next != this){
            if((result = next.allocObj0()) != null)return result;
            next = next.next;
        }
        return null;
    }


    private void onInsert(){
        parent.last = this;
        for(int size = this.size+1;!UNSAFE.compareAndSwapInt(this, sizeOffset, this.size, size);size+=1);
    }


    private void onTake(){
        parent.last = this;
        for(int size = this.size-1;!UNSAFE.compareAndSwapInt(this, sizeOffset, this.size, size);size-=1);
    }


    public final int size(){
        int curSize = this.size;
        ConcurrentCASAllocatorLE next = this.next;
        while (next != this){
            curSize += next.size;
            next = next.next;
        }
        return curSize;
    }


    public final boolean isEmpty(){
        if(this.size != 0)return false;
        ConcurrentCASAllocatorLE next = this.next;
        while (next != this){
            if(next.size != 0)return false;
            next = next.next;
        }
        return true;
    }


    public final int loopSize(){
        int curSize = 1;
        ConcurrentCASAllocatorLE next = this.next;
        while (next != this){
            ++curSize;
            next = next.next;
        }
        return curSize;
    }


    public final void freeObj(Object obj){
        ConcurrentCASAllocatorLE last;
        ConcurrentCASAllocatorLE parent;
        if((last = (parent = this.parent).last).freeObj0(obj))return;
        ConcurrentCASAllocatorLE next = last.next;
        while (next != last){
            if(next.freeObj0(obj))return;
            next = next.next;
        }
        if(loopSize() >= maxLoop)return;
        synchronized (this){
            ConcurrentCASAllocatorLE newArr = new ConcurrentCASAllocatorLE(parent.nodes.length, maxLoop);
            newArr.next = next.next;
            next.next = newArr;
            next.parent = parent;
        }
        if(!next.freeObj0(obj))freeObj(obj);
    }


    private boolean freeObj0(Object obj){
        Node[] nodes;
        int size;
        if((size = (nodes = this.nodes).length) > this.size) {
            int cur = cursor;
            for (int i = cur; i < size; i++) {
                if (nodes[i].tryPut(obj)) {
                    this.cursor = i;
                    onInsert();
                    return true;
                }
            }
            for (int i = 0; i < cur; i++) {
                if (nodes[i].tryPut(obj)) {
                    this.cursor = i;
                    onInsert();
                    return true;
                }
            }
        }
        return false;
    }


    private static class Node{
        private static final long nodeOffset = offset(Node.class, "value");
        private Object value;

        Object tryGet(){
            Object value;
            if((value = this.value) == null)return null;
            if(UNSAFE.compareAndSwapObject(this, nodeOffset, value, null))return value;
            return null;
        }

        boolean tryPut(Object obj){
            if(this.value != null)return false;
            return UNSAFE.compareAndSwapObject(this, nodeOffset, null, obj);
        }
    }
}