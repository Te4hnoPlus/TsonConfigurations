package plus.tson;

import plus.tson.security.ClassManager;
import plus.tson.utl.ByteStrBuilder;


/**
 * Alternative parser for creating a hierarchy of Tson objects from a CSV-like string
 */
public class TsonCSVParser {
    private final char[] data;
    private final ByteStrBuilder b = new ByteStrBuilder(16);
    private final TsonParser parser;

    public TsonCSVParser(String data) {
        this(new ClassManager.Def(), data.toCharArray());
    }


    public TsonCSVParser(ClassManager manager, char[] data) {
        parser = new TsonParser(manager, data);
        this.data = data;
    }


    public TsonList getList(){
        return getList(false);
    }


    public TsonList getList(boolean includeEmpty){

        TsonList list = new TsonList();
        TsonList line = new TsonList();


        for (int cursor = 0; cursor < data.length; cursor++) {
            char c = data[cursor];
            if(c == ' ' || c == '\t' || c == '\r'){
                continue;
            }
            if(c == '\n'){
                if(includeEmpty || !line.isEmpty()){
                    list.add(line);
                    line = new TsonList();
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
                        line = new TsonList();
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