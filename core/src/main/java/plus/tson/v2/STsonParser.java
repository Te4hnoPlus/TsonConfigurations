package plus.tson.v2;

import plus.tson.*;
import plus.tson.exception.TsonSyntaxException;
import plus.tson.security.ClassManager;
import plus.tson.utl.ByteStrBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Alternative parser for creating a hierarchy of Tson objects from a Json string
 * <br><br>
 * Usage example:
 * <pre>
 * {@code new TJsonParser("{\"key\": 10}").getMap()}
 * </pre>
 */
public final class STsonParser extends ByteStrBuilder{
    private final ClassManager manager;
    private final byte[] data;
    private int cursor = 0;

    public STsonParser(final String str){
        this(str, false, new ClassManager.Def());
    }

    public STsonParser(final String str, ClassManager manager) {
        this(str.getBytes(StandardCharsets.UTF_8), false, manager);
    }


    public STsonParser(final String str, final boolean objMode, ClassManager manager) {
        this(str.getBytes(StandardCharsets.UTF_8), objMode, manager);
    }


    public STsonParser(final byte[] data, final boolean objMode, ClassManager manager) {
        super(16);
        this.data = data;
        this.manager = manager;
    }


    public TsonMap compile(){
        final byte[] data = this.data;
        for (int i = 0, s = data.length; i < s;i++){
            if(data[i] == '{') return getMap(null, data, this.cursor = i+1);
        }
        return null;
    }


    private boolean skipCommentsIfNeed(final byte[] data, int cursor){
        if(data[cursor] == '/'){
            int len = data.length;
            if(len > (cursor+=1)){
                byte chr = data[cursor];
                if(chr == '/'){
                    cursor += 1;
                    while (chr != '\n' && cursor < len){
                        chr = data[++cursor];
                    }
                    this.cursor = cursor+1;
                    return true;
                }
                if(chr == '*'){
                    cursor += 1;
                    while (cursor < len){
                        if(data[cursor] == '/' && data[cursor-1] == '*'){
                            this.cursor = cursor+1;
                            return true;
                        }
                        ++cursor;
                    }
                }
            }
        }
        return false;
    }


    private TsonMap getMap(Object ctx, final byte[] data, final int cursor){
        final TsonMap map;
        fillMap(ctx, map = new TsonMap(), data, cursor);
        return map;
    }


    private void fillMap(Object ctx, final Map<String,TsonObj> map, final byte[] data, int cursor){
        final int length = data.length;
        boolean waitSep = false, waitKey = true;
        String key = null;
        for (byte chr; cursor < length; cursor++){
            if(skipCommentsIfNeed(data, cursor)){
                cursor = this.cursor-1;
            }
            if((chr = data[cursor]) == '}'){
                this.cursor = cursor+1;
                return;
            }
            if(chr == ' ' || chr == '\n')continue;
            if(waitSep) {
                if(chr == ',') waitSep = false;
            }else if(waitKey){
                key = getObjKey(data, cursor);
                if(key != null) {
                    cursor = this.cursor;
                } else {
                    --cursor;
                }
                waitKey = false;
            } else {
                this.cursor = cursor;
                TsonObj item = getItem(ctx, data, chr);
                if(key != null) map.put(key, item);
                cursor = this.cursor-1;
                waitSep = true;
                waitKey = true;
            }
        }
    }


    private TsonList getList(final Object ctx, final byte[] data, int cursor){
        final TsonList list;
        fillList(ctx, list = new TsonList(), data, cursor, (byte) ']');
        return list;
    }


    private void fillList(final Object ctx, final TsonList list, final byte[] data, int cursor, byte end){
        boolean waitSep = false;
        byte chr;
        for (final int length = data.length; cursor < length; cursor++){
            if(skipCommentsIfNeed(data, cursor)){
                cursor = this.cursor-1;
            }
            if((chr = data[cursor]) == end){
                this.cursor = cursor+1;
                return;
            }
            if(chr == ' ' || chr == '\n')continue;
            if(waitSep) {
                if(chr == ',') waitSep = false;
                continue;
            }
            this.cursor = cursor;
            list.add(getItem(ctx, data, chr));
            cursor = this.cursor-1;
            waitSep = true;
        }
    }


    public TsonObj getItem(Object ctx, final byte[] data, final byte chr){
        switch (chr){
            case '"' : return getStr(data,'"');
            case '\'': return getStr(data,'\'');
            case '{' : return getMap(ctx, data, ++cursor);
            case '[' : return getList(ctx, data, ++cursor);
            case '-' : return getNum(data, ++cursor, true);
            default: {
                if(chr >= '0' && chr <= '9')return getNum(data, cursor, false);
                if(isTrue(data, cursor)){
                    cursor += 3;
                    return TsonBool.TRUE;
                }
                if(isFalse(data, cursor)){
                    cursor += 4;
                    return TsonBool.FALSE;
                }
                if(isNew(data, cursor)){
                    cursor += 4;
                    return getCustom(ctx, data);
                }
                if(isThis(data, cursor)){
                    if(data[cursor+4] == '.'){
                        if(ctx == null){
                            throw TsonSyntaxException.make(cursor, data, "Null pointer 'this'");
                        }
                        cursor += 5;
                        return invokeCustom(ctx, data, null);
                    } else {
                        cursor += 4;
                        if(ctx == null)return TsonField.NULL;
                        return new TsonField<>(ctx);
                    }
                }
                throw TsonSyntaxException.make(cursor, data,"Unknown token '"+((char)data[cursor])+"'");
            }
        }
    }


    private TsonObj invokeCustom(final Object ctx, final byte[] data, final AtomicBoolean isVoid){
        String methodName = readMethodName(data, cursor);
        if(data[cursor] == '('){
            int lastCharName = cursor;
            Object[] args = readMethodArgs(ctx, data, ++this.cursor);
            try {
                return TsonObj.wrap(
                        manager.invoke(ctx, methodName, isVoid, args)
                );
            } catch (Exception e){
                throw TsonSyntaxException.make(lastCharName, data, e);
            }
        } else {
            throw TsonSyntaxException.make(cursor, data);
        }
    }


    private Object[] readMethodArgs(final Object ctx, final byte[] data, final int cursor){
        TsonList constArgs = new TsonList();
        fillList(ctx, constArgs, data, cursor, (byte) ')');
        int size = constArgs.size();
        if(size == 0)return null;
        Object[] args = new Object[size];
        for (int i = 0; i < size; i++) {
            args[i] = constArgs.get(i).getField();
        }
        return args;
    }


    private TsonObj getCustom(final Object ctx, final byte[] data){
        String clazzName = readMethodName(data, cursor);

        int lastCharName = cursor-1;
        Object inst;
        try {
            if (data[cursor] == '(') {
                inst = manager.newInstance(
                        clazzName,
                        readMethodArgs(ctx, data, ++cursor)
                );
            } else {
                inst = manager.newInstance(clazzName);
            }
        } catch (Exception e){
            if(e instanceof TsonSyntaxException){
                throw (TsonSyntaxException)e;
            }
            throw TsonSyntaxException.make(lastCharName, data, e);
        }

        if(data[cursor] == '{'){
            lastCharName = cursor-1;
            try {
                Proxy proxy = new Proxy(inst);
                fillMap(inst, proxy, data, ++cursor);
            } catch (Exception e) {
                if(e instanceof TsonSyntaxException){
                    throw (TsonSyntaxException)e;
                }
                throw TsonSyntaxException.make(lastCharName, data, e);
            }
        }
        while(data[cursor] == '.'){
            lastCharName = cursor-1;
            ++this.cursor;
            try {
                AtomicBoolean isVoid = new AtomicBoolean(false);
                TsonObj res = invokeCustom(inst, data, isVoid);
                if(!isVoid.get())inst = res.getField();
            } catch (Exception e) {
                if(e instanceof TsonSyntaxException){
                    throw (TsonSyntaxException)e;
                }
                throw TsonSyntaxException.make(lastCharName, data, e);
            }
        }
        return TsonObj.wrap(inst);
    }


    private static final class Proxy implements Map<String,TsonObj>{
        private final Class<?> clazz;
        private final Object inst;

        private Proxy(Object inst) {
            this.inst = inst;
            this.clazz = inst.getClass();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public TsonObj get(Object key) {
            return null;
        }


        @Override
        public TsonObj put(String key, TsonObj value) {

            try {
                Field field = clazz.getDeclaredField(key);
                boolean isAccessible = field.isAccessible();

                if (!isAccessible) {
                    field.setAccessible(true);
                }

                insert(inst, field, value);

                if (!isAccessible) {
                    field.setAccessible(false);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return null;
        }


        @Override
        public TsonObj remove(Object key) {
            throw new UnsupportedOperationException();
        }


        @Override
        public void putAll(Map<? extends String, ? extends TsonObj> m) {
            for (Map.Entry<? extends String, ? extends TsonObj> entry : m.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }


        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }


        @Override
        public Set<String> keySet() {
            return Set.of();
        }


        @Override
        public Collection<TsonObj> values() {
            return List.of();
        }


        @Override
        public Set<Entry<String, TsonObj>> entrySet() {
            return Set.of();
        }
    }


//    private static void fillFields0(Object inst, TsonMap fieldsMap) throws NoSuchFieldException, IllegalAccessException {
//        Class<?> clazz = inst.getClass();
//        for (Map.Entry<String,TsonObj> entry:fieldsMap.entrySet()){
//            Field field = clazz.getDeclaredField(entry.getKey());
//            field.setAccessible(true);
//
//            insert(inst, field, entry.getValue());
//        }
//    }


    private static void insert(Object inst, Field field, TsonObj obj) throws IllegalAccessException {
        if(Modifier.isStatic(field.getModifiers())){
            inst = null;
        }
        switch (obj.type()){
            case BOOL:{
                field.set(inst, obj.getBool());
                break;
            }
            case INT:{
                field.set(inst, obj.getInt());
                break;
            }
            case LONG:{
                field.set(inst, obj.getLong());
            }
            case FLOAT:{
                field.set(inst, obj.getFloat());
                break;
            }
            case DOUBLE:{
                field.set(inst, obj.getDouble());
            }
            default:{
                field.set(inst, obj.getField());
            }
        }
    }


    private String readMethodName(final byte[] data, int cursor){
        clear();
        byte c;
        for(final int length = data.length; cursor < length; ++cursor){
            if((c = data[cursor]) == '(' || c == '{') break;
            if(c == ' ' || c == '\n')continue;
            append(c);
        }
        this.cursor = cursor;
        return cString();
    }

//    private String tempName;
//    private int    tempType;
//    private void rescanType(final byte[] data){
//        int cur = cursor;
//        byte chr = data[cur];
//        while (
//                (chr >= 'a' && chr <= 'z') ||
//                (chr >= 'A' && chr <= 'Z') ||
//                (chr >= '0' && chr <= '9')
//        ){
//            chr = data[++cur];
//        }
//    }


    private TsonObj getNum(final byte[] data, int cursor, final boolean invert){
        boolean sep = false;
        final int length = data.length;
        int num = 0;
        byte size = 0;

        byte c;
        for (;cursor < length && size < 9; cursor++){
            if((c = data[cursor]) > 47 && c < 58) {
                num = (num * 10) + (c-48);
                ++size;
                continue;
            }
            if(c == '_')continue;
            if(c == '.'){
                ++cursor;
                sep = true;
            }
            break;
        }
        if(size == 9){
            long lNum = num;
            for (;cursor < length; cursor++, size++){
                if(size > 18)throw TsonSyntaxException.make(cursor, data, "Number '"+lNum+((char)data[cursor])+"..' is too big");
                if((c = data[cursor]) > 47 && c < 58) {
                    num = num * 10 + (c-48);
                    ++size;
                }
                if(c == '_')continue;
                if(c == '.'){
                    ++cursor;
                    sep = true;
                    break;
                }
            }
            if(sep){
                return readDel(data, cursor, num, size, invert);
            } else {
                this.cursor = cursor;
                if(invert)return new TsonLong(-lNum);
                return new TsonLong(lNum);
            }
        }

        if(sep){
            return readDel(data, cursor, num, size, invert);
        } else {
            this.cursor = cursor;
            if(invert)return new TsonInt(-num);
            return new TsonInt(num);
        }
    }


    private TsonObj readDel(final byte[] data, int cursor, long lNum, byte size, final boolean invert){
        final int length = data.length;
        int pSize = 0, nSize = 1;

        for (byte c ;cursor < length; cursor++){
            if((c = data[cursor]) > 47 && c < 58) {
                if(++size > 18){
                    throw TsonSyntaxException.make(cursor, data, "Number '"+(((double)lNum)/pSize)+((char)data[cursor])+"..' is too big");
                }
                lNum = lNum * 10 + (c-48);
                pSize = nSize;
                nSize *= 10;
                continue;
            }
            if(c == '_')continue;
            break;
        }
        this.cursor = cursor;
        if(size > 8){
            if(invert)return new TsonDouble(-(((double)lNum)/pSize));
            return new TsonDouble(((double)lNum)/pSize);
        } else {
            if(invert)return new TsonFloat(-((float)(((double)lNum)/pSize)));
            return new TsonFloat((float)(((double)lNum)/pSize));
        }
    }


    private static boolean isFalse(final byte[] data, final int cursor){
        final byte cur = data[cursor];
        if(cur == 'f')
            return data[cursor + 1] == 'a' && data[cursor + 2] == 'l' && data[cursor + 3] == 's' && data[cursor + 4] == 'e';
        if(cur == 'F'){
            if(data[cursor + 1] == 'a')
                return data[cursor + 2] == 'l' && data[cursor + 3] == 's' && data[cursor + 4] == 'e';
            else if(data[cursor + 1] == 'A')
                return data[cursor + 2] == 'L' && data[cursor + 3] == 'S' && data[cursor + 4] == 'E';
        }
        return false;
    }


    private static boolean isTrue(final byte[] data, final int cursor){
        final byte cur = data[cursor];
        if(cur == 't')
            return data[cursor + 1] == 'r' && data[cursor + 2] == 'u' && data[cursor + 3] == 'e';
        if(cur == 'T'){
            if(data[cursor+1] == 'r')
                return data[cursor + 2] == 'u' && data[cursor + 3] == 'e';
            else if(data[cursor+1] == 'R')
                return data[cursor + 2] == 'U' && data[cursor + 3] == 'E';
        }
        return false;
    }


    private static boolean isNew(final byte[] data, final int cursor){
        return data[cursor] == 'n' && data[cursor + 1] == 'e' && data[cursor + 2] == 'w' && data[cursor + 3] == ' ';
    }


    private static boolean isThis(final byte[] data, final int cursor){
        return data[cursor] == 't' && data[cursor + 1] == 'h' && data[cursor + 2] == 'i' && data[cursor + 3] == 's';
    }


    private TsonStr getStr(final byte[] data, final char end){
        final int length = data.length;
        clear();
        int cur = cursor+1;
        for(byte c; cur < length; ++cur){
            if((c = data[cur]) == end)break;
            if(c == '\\'){
                ++cur;
                continue;
            }
            append(c);
        }
        this.cursor = cur;
        return new TsonStr(cString());
    }


    private String getObjKey(final byte[] data, int cursor){
        clear();
        byte c;
        for(final int length = data.length; cursor < length; ++cursor){
            if((c = data[cursor]) == '=') break;
            if(c == ',' || c == '}'){
                //this.cursor = cursor;
                return null;
            }
            if(c == ' ' || c == '\n')continue;
            append(c);
        }
        this.cursor = cursor;
        return cString();
    }
}