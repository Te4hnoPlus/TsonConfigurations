package plus.tson;

import plus.tson.security.ClassManager;
import java.io.File;


public class TJsonFile extends TsonFile{
    private int indent = 4, maxInline = 32;
    private final boolean objMode;

    public TJsonFile(String fileName, boolean objMode) {
        this(new File(fileName), objMode);
        load();
    }


    @Override
    public TJsonFile load(){
        return (TJsonFile) load((ClassManager) null);
    }


    @Override
    public TJsonFile load(ClassManager manager, String def) {
        return (TJsonFile) super.load(manager, def);
    }


    public TJsonFile(File file, boolean objMode) {
        super(file);
        this.objMode = objMode;
    }


    public TJsonFile(String fileName) {
        this(fileName, false);
    }


    public TJsonFile(File file) {
        this(file, false);
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
        new TJsonParser(data, objMode).fillMap(this);
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