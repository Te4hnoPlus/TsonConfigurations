package plus.tson;

import plus.tson.utl.Te4HashMap;
import java.util.Map;


/**
 * Json writer with indent
 */
public class TJsonWriter {
    private final StringBuilder builder = new StringBuilder();
    private Te4HashMap<TsonObj, Integer>[] sizeCache = null;
    private final int indent;
    private final TsonObj src;
    private int curIndent;
    private final boolean objMode;
    private final int maxInline;

    public TJsonWriter(TsonObj src){
        this(src, 4, 32, true);
    }


    public TJsonWriter(TsonObj src, int indent, boolean objMode) {
        this(src, indent, 0, objMode);
    }


    public TJsonWriter(TsonObj src, int indent, int maxInline, boolean objMode) {
        this.indent = indent;
        this.src = src;
        this.objMode = objMode;
        this.maxInline = maxInline;
    }


    private void code(TsonObj obj){
        switch (obj.type()){
            case MAP: {
                codeMap(obj.getMap());
                return;
            }
            case LIST: {
                codeList(obj.getList());
                return;
            }
            default:{
                codeDef(obj);
                return;
            }
        }
    }


    private Te4HashMap<TsonObj, Integer> sizeCache(int n){
        Te4HashMap<TsonObj, Integer>[] res = sizeCache;
        if(res == null)return
                (sizeCache = new Te4HashMap[]{new Te4HashMap<>(), new Te4HashMap<>()})[n];
        return res[n];
    }


    private int sizeOf(TsonObj obj){
        switch (obj.type()){
            case MAP: {
                Te4HashMap<TsonObj, Integer> cache;
                Integer res = (cache = sizeCache(0)).get(obj);
                if(res != null)return res;

                int curSize = 2;
                boolean objMode = this.objMode;
                for (Map.Entry<String, TsonObj> node : obj.getMap().entrySet()) {
                    curSize += node.getKey().length();
                    if(!objMode)curSize += 2;
                    curSize += sizeOf(node.getValue());
                }
                cache.put(obj, res);
                return curSize;
            }
            case LIST:{
                Te4HashMap<TsonObj, Integer> cache;
                Integer res = (cache = sizeCache(1)).get(obj);
                if(res != null)return res;

                int curSize = 1;
                for (TsonObj obj0: obj.getList()){
                    curSize += sizeOf(obj0);
                    curSize += 1;
                }
                cache.put(obj, res);
                return curSize;
            }
            default:
                return obj.toJsonObj().length();
            case STRING:
                return obj.getStr().length()+2;
        }
    }


    private void codeDef(TsonObj obj){
        if(objMode) obj.codeJsonObj(builder);
        else obj.codeJson(builder);
    }


    private char[] empty(){
        int cur = curIndent;
        char[] chars = new char[cur];
        for (int i = 0; i < cur; i++)chars[i] = ' ';
        return chars;
    }


    protected char keyChar(){
        return ':';
    }


    private void codeMap(TsonMap map){
        StringBuilder builder = this.builder;
        if(map.isEmpty()){
            builder.append("{}");
            return;
        }

        char kc = keyChar();

        if(maxInline > 0){
            int size = sizeOf(map);
            if(size <= maxInline){
                if(objMode)codeJsonObj(map, builder, kc);
                else codeJson(map, builder, kc);
                return;
            }
        }

        curIndent += indent;

        char[] empty = empty();
        builder.append("{\n").append(empty);

        if(objMode) {
            for (Map.Entry<String, TsonObj> node : map.entrySet()) {
                builder.append(node.getKey()).append(kc).append(' ');
                code(node.getValue());
                builder.append(',').append('\n').append(empty);
            }
        } else {
            for (Map.Entry<String, TsonObj> node : map.entrySet()) {
                builder.append('"').append(node.getKey()).append('"').append(kc).append(' ');
                code(node.getValue());
                builder.append(',').append('\n').append(empty);
            }
        }
        curIndent -= indent;

        builder.setLength(builder.length() - empty.length - 2);
        builder.append('\n').append(empty, 0, empty.length-indent).append('}');
    }


    private void codeList(TsonList list){
        int size = list.size();
        StringBuilder builder = this.builder;

        if(size == 0){
            builder.append("[]");
        } else {
            if(maxInline > 0){
                int size0 = sizeOf(list);
                if(size0 <= maxInline){
                    if(objMode)list.codeJsonObj(builder);
                    else list.codeJson(builder);
                    return;
                }
            }

            curIndent += indent;
            char[] empty = empty();

            builder.append("[\n").append(empty);

            for (int i = 0; i < size; i++) {
                code(list.get(i));
                builder.append(',').append('\n').append(empty);
            }
            curIndent -= indent;

            builder.setLength(builder.length() - empty.length - 2);
            builder.append('\n').append(empty, 0, empty.length-indent).append(']');
        }
    }


    protected static void codeJson(TsonMap map, StringBuilder builder, char kc){
        if(map.isEmpty()){
            builder.append("{}");
        } else {
            builder.append('{');
            for (Map.Entry<String, TsonObj> node : map.entrySet()) {
                builder.append('"').append(node.getKey()).append('"').append(kc);
                node.getValue().codeJson(builder);
                builder.append(',');
            }
            builder.setCharAt(builder.length() - 1, '}');
        }
    }


    protected static void codeJsonObj(TsonMap map, StringBuilder builder, char kc) {
        if(map.isEmpty()){
            builder.append("{}");
        } else {
            builder.append('{');
            for (Map.Entry<String, TsonObj> node : map.entrySet()) {
                builder.append(node.getKey()).append(kc);
                node.getValue().codeJsonObj(builder);
                builder.append(',');
            }
            builder.setCharAt(builder.length() - 1, '}');
        }
    }


    @Override
    public String toString(){
        code(src);
        String result = builder.toString();
        builder.setLength(0);
        return result;
    }
}