package plus.tson.exception;

import plus.tson.TsonObj;


public class WrongTypeException extends RuntimeException {
    public WrongTypeException(Class<? extends TsonObj> clazz, String mtd) {
        super("["+clazz.getSimpleName()+"] Not supported \""+mtd+"\"");
    }
}