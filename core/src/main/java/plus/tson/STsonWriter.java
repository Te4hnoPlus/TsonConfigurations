package plus.tson;


public class STsonWriter extends TJsonWriter{
    public STsonWriter(TsonObj src){
        this(src, 4, 32);
    }


    public STsonWriter(TsonObj src, int indent, int maxInline) {
        super(src, indent, maxInline, true);
    }


    @Override
    protected char keyChar() {
        return '=';
    }
}