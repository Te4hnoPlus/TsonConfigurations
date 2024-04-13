package plus.tson.utl;

import java.lang.reflect.Array;


public abstract class Allocator<T> {
    private T[] data;
    int cursor = -1;


    public Allocator(){
        T obj = newObj();
        data = (T[]) Array.newInstance(obj.getClass(), 8);
    }


    public T malloc(){
        if(cursor==-1)return newObj();
        T obj = data[cursor];
        data[cursor--] = null;
        return obj;
    }


    public void free(T obj){
        if(++cursor>=data.length){
            T[] newData = (T[]) Array.newInstance(obj.getClass(), data.length+8);
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
        data[cursor] = obj;
    }


    protected abstract T newObj();
}
