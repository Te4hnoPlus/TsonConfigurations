package plus.tson;

import plus.tson.security.ClassManager;


/**
 * Alternative parser for creating a hierarchy of Tson objects from a CSV-like string
 */
public class TsonCSVParser {
    private final char[] data;
    private final TsonParser parser;
    private final int defLineSize;

    public TsonCSVParser(String data) {
        this(new ClassManager.Def(), data.toCharArray(), 10);
    }


    public TsonCSVParser(ClassManager manager, String data) {
        this(manager, data.toCharArray(), 10);
    }


    public TsonCSVParser(ClassManager manager, char[] data) {
        this(manager, data, 10);
    }


    public TsonCSVParser(ClassManager manager, char[] data, int defLineSize) {
        parser = new TsonParser(manager, data);
        this.data = data;
        this.defLineSize = defLineSize;
    }


    public TsonList getList(){
        return getList(false);
    }


    public TsonList getList(boolean includeEmpty){

        TsonList list = new TsonList();
        TsonList line = new TsonList(defLineSize);


        for (int cursor = 0; cursor < data.length; cursor++) {
            char c = data[cursor];
            if(c == ' ' || c == '\t' || c == '\r'){
                continue;
            }
            if(c == '\n'){
                if(includeEmpty || !line.isEmpty()){
                    list.add(line);
                    line = new TsonList(defLineSize);
                }
                continue;
            }

            parser.cursor = cursor;
            TsonObj item = parser.getAutho();
            if(item != null){
                line.add(item);
            }
            cursor = parser.cursor;
            while (cursor < data.length) {
                c = data[cursor];
                if(c == '\n'){
                    if(includeEmpty || !line.isEmpty()){
                        list.add(line);
                        line = new TsonList(defLineSize);
                    }
                    break;
                }
                ++cursor;
                if(c == ' ' || c == '\t' || c == '\r')continue;
                if(c == ',')break;
            }
        }

        if(includeEmpty || !line.isEmpty())
            list.add(line);
        return list;
    }
}