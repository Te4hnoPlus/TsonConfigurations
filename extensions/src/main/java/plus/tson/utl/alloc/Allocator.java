package plus.tson.utl.alloc;


public interface Allocator {
    int size();


    boolean isEmpty();


    Object tryAllocRaw();


    void freeObj(Object obj);
}
