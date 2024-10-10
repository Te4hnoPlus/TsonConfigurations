package plus.tson.utl.alloc;


/**
 * Objects allocator interface
 * Designed to store objects that can be reused in the future instead of creating new instances
 */
public interface Allocator {
    /**
     * @return Probable available objects
     */
    int size();


    /**
     * @return true if allocator is empty, and cant return self object
     */
    boolean isEmpty();


    /**
     * Try to allocate object
     * @return allocated object or null on fail
     */
    Object tryAllocRaw();


    /**
     * Put object to allocator
     * Once an object is placed, there is some chance that the object can be returned.
     * See {@link #tryAllocRaw()}
     * <p>
     * Order and sequence are not implied or guaranteed
     * @param obj object to put
     */
    void freeObj(Object obj);
}