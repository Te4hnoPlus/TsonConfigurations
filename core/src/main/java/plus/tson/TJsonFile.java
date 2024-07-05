package plus.tson;

import plus.tson.security.ClassManager;
import java.io.File;


public class TJsonFile extends TsonFile{
    private int indent = 4, maxInline = 32;
    private boolean objMode = true;

    public TJsonFile(String fileName) {
        super(fileName);
    }


    public TJsonFile(ClassManager manager, String fileName) {
        super(manager, fileName);
    }


    public TJsonFile(File file) {
        super(file);
    }


    public TJsonFile indent(int indent){
        this.indent = indent;
        return this;
    }


    public TJsonFile objMode(boolean mode){
        this.objMode = mode;
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
}