package plus.tson;

import plus.tson.security.ClassManager;
import java.io.File;


public class TJsonFile extends TsonFile{
    private int indent = 4, maxInline = 32;
    private final boolean objMode;

    public TJsonFile(String fileName, boolean objMode) {
        super(fileName);
        this.objMode = objMode;
    }


    public TJsonFile(File file, boolean objMode) {
        super(file);
        this.objMode = objMode;
    }


    public TJsonFile(String fileName) {
        this(fileName, true);
    }


    public TJsonFile(File file) {
        this(file, true);
    }


    public TJsonFile indent(int indent){
        this.indent = indent;
        return this;
    }


    public TJsonFile maxInline(int max){
        this.maxInline = max;
        return this;
    }


    @Override
    protected void parse(ClassManager manager, String data) {
        TJsonParser parser = new TJsonParser(data, objMode);
        parser.goTo('{');
        parser.fillMap(this);
    }


    @Override
    public TJsonFile save(){
        String str = new TJsonWriter(this, indent, maxInline, objMode).toString();
        write(getFile(), str);
        return this;
    }


    @Override
    public TJsonFile clone() {
        return (TJsonFile) super.clone();
    }
}