package plus.tson.exception;

import plus.tson.TsonObj;


/**
 * Called when current Tson operation is not supported
 */
public class WrongTypeException extends RuntimeException {
    public WrongTypeException(Class<? extends TsonObj> clazz, String mtd) {
        super("["+clazz.getSimpleName()+"] Not supported \""+mtd+"\"");
    }
}