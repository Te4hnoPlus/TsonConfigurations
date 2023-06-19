package plus.tson.security;

import plus.tson.exception.NoSearchException;


public interface ClassManager {
    final class Def implements ClassManager{}
    default String getAllowedPath(){
        return "";
    }
    default Class<?> forName(String clazz){
        try {
            return Class.forName(getAllowedPath()+clazz);
        } catch (ClassNotFoundException e) {
            throw new NoSearchException(clazz+" not class");
        }
    }
}