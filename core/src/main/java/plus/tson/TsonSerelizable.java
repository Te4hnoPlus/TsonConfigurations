package plus.tson;


/**
 * The preferred type of object that can be located in TsonField
 */
public interface TsonSerelizable {
    TsonObj toTson();
}