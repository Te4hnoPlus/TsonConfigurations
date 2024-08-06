package plus.tson;

import plus.tson.exception.TsonSyntaxException;
import plus.tson.security.ClassManager;
import plus.tson.utl.ByteStrBuilder;
import plus.tson.utl.FuncCompiler;
import plus.tson.utl.Te4HashMap;
import plus.tson.utl.Tuple;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Alternative smart parser for creating a hierarchy of Tson objects from a STson string
 * <br><br>
 * Usage example:
 * <pre>
 * {@code
int length = new STsonParser("""
{
obj = new TestV2('test1', 'test2'){//create an object
field1 = 'test3',                  //insert to field 'field1' value 'test3'
field2 = this.test2('test4'),      //insert to field 'field2' result of call method 'this.test2(..)'
this.test3()                       //call method 'this.test3()'
}.name()                           //call methods over the result
.length()                          //if method return void -> return 'this'

}"""
).compile().getInt("obj");
 * }
 * </pre>
 */
public class STsonParser extends ByteStrBuilder{
    private final ClassManager manager;
    private final byte[] data;
    private Proxy proxy;
    private int cursor = 0;
    private TsonFunc.Compiler compiler;
    private Imports imports;

    public STsonParser(final String str){
        this(str, new ClassManager.Def());
    }

    public STsonParser(final String str, ClassManager manager) {
        this(str.getBytes(StandardCharsets.UTF_8), manager);
    }


    public STsonParser with(TsonFunc.Compiler compiler){
        this.compiler = compiler;
        return this;
    }


    public STsonParser with(Imports imports){
        if(this.imports != null){
            for (Map.Entry<Byte, ArrayList<Tuple<byte[],String>>> entry:this.imports.entrySet()){
                for (Tuple<byte[],String> pair:entry.getValue()){
                    imports.add(pair.B, pair.A, 0, pair.A.length);
                }
            }
        } else {
            this.imports = imports;
        }
        return this;
    }


    public STsonParser(final byte[] data, ClassManager manager) {
        super(16);
        this.data = data;
        this.manager = manager;
    }


    public STsonParser readImports(){
        if(imports == null) imports = new Imports();
        byte[] data = this.data;
        boolean hasEngine = false;
        for (int cursor = this.cursor;cursor < data.length; cursor++){
            byte chr = data[cursor];
            if(chr == '{')return this;
            if(chr == ' ' || chr == '\r' || chr == '\n' || chr == '\t')continue;
            if(isImport(data, cursor)){
                readImport(data, this.cursor = cursor+7);
                cursor = this.cursor-1;
            } else {
                if(!hasEngine) {
                    if (isEngine(data, cursor)) {
                        readEngine(data, this.cursor = cursor + 7);
                        cursor = this.cursor - 1;
                        hasEngine = true;
                    }
                }
            }
        }
        return this;
    }


    private void readEngine(final byte[] data, final int cursor){
        final int length = data.length;
        int cur = cursor+1;
        for(byte c; cur < length; ++cur){
            if((c = data[cur]) == '\n' || c == ';')break;
        }
        String name = new String(data, cursor, cur-cursor, StandardCharsets.UTF_8);
        if(compiler != null){
            TsonFunc.Compiler compiler;
            try {
                compiler = this.compiler.fork();
            } catch (Exception e){
                throw TsonSyntaxException.make(cursor, data, "Engine ["+this.compiler+"] cant be fork");
            }
            if(compiler instanceof TsonFunc.Reflector){
                TsonFunc.Reflector reflector = (TsonFunc.Reflector) compiler;
                reflector.tryInstallEngine(name);
            }
        } else {
            throw TsonSyntaxException.make(cursor, data, "Can`t fork null compiler");
        }
    }


    private void readImport(final byte[] data, final int cursor){
        final int length = data.length;
        int cur = cursor+1;
        for(byte c; cur < length; ++cur){
            if((c = data[cur]) == '\n' || c == ';')break;
        }
        this.cursor = cur+1;
        imports.add(null, data, cursor, cur);
    }


    public TsonMap getMap(){
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
                key = getKey(data, cursor);
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
        return processCalls(getItemRaw(ctx, data, chr), data);
    }


    private Object getItemRaw(Object ctx, final byte[] data, final byte chr){
        switch (chr){
            case '"' : return getStr(data, cursor, (byte) '"');
            case '\'': return getStr(data, cursor, (byte) '\'');
            case '{' : return getMap(ctx, data, ++cursor);
            case '[' : return getList(ctx, data, ++cursor);
            case '-' : return getNum(data, ++cursor, true);
            case '(' : return readClassOrBool(data, ++cursor);
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
                if(isFunc(data, cursor)){
                    cursor += 4;
                    return getFunc(ctx, data);
                }
                if(isThis(data, cursor)){
                    byte chr2 = data[cursor+4];
                    if(chr2 == '.'){
                        if(ctx == null){
                            throw TsonSyntaxException.make(cursor, data, "Null pointer 'this'");
                        }
                        cursor += 5;
                        return invokeCustom(ctx, data, null);
                    } else if(chr2 == ':' && data[cursor+5] == ':'){
                        cursor += 6;
                        //TODO
                        return readLambda(ctx, data);
                    } else {
                        cursor += 4;
                        if(ctx == null)return TsonField.NULL;
                        return new TsonField<>(ctx);
                    }
                }

                Object obj = readU(ctx, data);
                if(obj != null)return obj;

                throw TsonSyntaxException.make(cursor, data,"Unknown token '"+((char)data[cursor])+"'");
            }
        }
    }


    private Object readU(Object ctx, final byte[] data){
        String name = readToSpecial(data);

        byte chr = data[cursor];
        if(chr == ':' && data[cursor+1] == ':'){
            if(imports != null){
                name = imports.findFull(name);
            }

            cursor += 2;
            String mtd = readToSpecial(data);
            return compiler.compile(manager.forName(name), mtd);
        }
        if(chr == '('){
            int index = name.lastIndexOf('.');
            if(index == -1)return null;
            String methodName = name.substring(index+1);
            name = name.substring(0, index);
            if(imports != null){
                name = imports.findFull(name);
            }
            Object[] args = readMethodArgs(ctx, data, ++this.cursor);
            try {
                return manager.invoke(
                        manager.forName(name), null, methodName, args
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }


    public static class Imports extends Te4HashMap<Byte, ArrayList<Tuple<byte[],String>>>{
        public void add(String path){
            byte[] bytes = path.getBytes(StandardCharsets.UTF_8);
            add(path, bytes, 0, bytes.length);
        }


        private void add(byte[] bytes, int from, int to){
            add(null, bytes, from, to);
        }


        private void add(String str, byte[] bytes, int from, int to){
            byte key = bytes[to-1];
            ArrayList<Tuple<byte[],String>> list = super.get(key);
            if(list == null){
                super.fput(key, list = new ArrayList<>());
            }
            //list.add(new String(bytes, from, to-from, StandardCharsets.UTF_8));
            byte[] imp = new byte[to-from];
            System.arraycopy(bytes, from, imp, 0, to-from);
            if(str == null) str = new String(imp, StandardCharsets.UTF_8);
            list.add(new Tuple<>(imp, str));
        }


        public String findFull(String str){
            byte[] bytes = str.getBytes();
            String res = find(bytes, 0, bytes.length-1);
            if(res == null)return str;
            return res;
        }


        public String find(byte[] bytes, int from, int to){
            byte key = bytes[to];
            ArrayList<Tuple<byte[],String>> list = super.get(key);
            if(list == null){
                return null;
            }
            for (Tuple<byte[],String> pair:list){
                byte[] im = pair.A;
                if(isEqualEnd(im, bytes, from, to)){
                    if(im[im.length-to+from-2] == '.') return pair.B;
                }
            }
            return null;
        }
    }
//
//
//    public static void main(String[] args) {
//        byte[] im = "plus.1Test".getBytes(StandardCharsets.UTF_8);
//        byte[] bytes = "  .1Test".getBytes(StandardCharsets.UTF_8);
//        int from = 2;
//        int to = bytes.length;
//
//        System.out.println(im[im.length-to+from] == '.');
//        System.out.println(bytes[from] == im[im.length-to+from]);
//
//        System.out.println(isEqualEnd(im, bytes, from, to-1));
//    }


    private static boolean isEqualEnd(byte[] src, byte[] check, int checkFrom, int checkTo){
        int len = src.length-1;
        int c = 0;
        for (int i = checkTo; i >= checkFrom; i--, c++){
            if(check[i] != src[len-c])return false;
        }
        return true;
    }


    private Object readLambda(Object ctx, byte[] data){
        return compiler.compile(ctx, readToSpecial(data));
    }


    private String readToSpecial(byte[] data){
        clear();
        byte c;
        int cursor = this.cursor;
        for(final int length = data.length; cursor < length; ++cursor){
            if(isSpecial(c = data[cursor])) break;
            append(c);
        }
        this.cursor = cursor;
        if(super.getLength() == 0)return "";
        return cString();
    }


    private static boolean isSpecial(byte c){
        return c == ')' || c == '}' || c == ' ' || c == '\n' || c == '(' || c == '{' || c == ',' || c == ':';
    }


    private Object getFunc(Object ctx, byte[] data) {
        //todo
        ArrayList<String> args = new ArrayList<>();
        int from = cursor;
        int to = data.length;

        for (int cur = from; cur < to; cur++){
            byte chr = data[cur];
            if(chr == ')'){
                ++cur;
                while (data[cur] != '{' && cur < to)++cur;
                from = ++cur;
                break;
            }
            if(chr == '(' || chr == ','){
                chr = data[++cur];
                while (chr == ' ' || chr == '\n' || chr == '\r' || chr == '\t'){
                    chr = data[++cur];
                }
                cursor = cur;
                args.add(readToSpecial(data));
                cur = this.cursor-1;
            }
        }
        //todo read args
        String[] args0 = args.toArray(new String[0]);

        int opens = 1;
        for (int cur = from; cur < to; cur++){
            if(skipCommentsIfNeed(data, cur)){
                cur = this.cursor-1;
            }
            byte chr = data[cur];
            if(chr == '\'' || chr == '"'){
                skipStr(data, cur, chr);
                cur = cursor-1;
                continue;
            }
            if(chr == '{'){
                opens += 1;
                continue;
            }
            if(chr == '}'){
                opens -= 1;
                if(opens == 0){
                    to = cur;
                    cursor = cur+1;
                    break;
                }
            }
        }
        TsonFunc.Frame frame = new TsonFunc.Frame(ctx, data, from, to, args0);
        if(compiler == null)return frame;
        try {
            return compiler.compile(frame);
        } catch (Exception e){
            e.printStackTrace();
            return frame;
        }
    }


    private void skipStr(final byte[] data, final int cursor, final byte end){
        final int length = data.length;
        int cur = cursor+1;
        for(byte c; cur < length; ++cur){
            if((c = data[cur]) == end)break;
            if(c == '\\') ++cur;
        }
        this.cursor = cur+1;
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


    private Object getCustom(final Object ctx, final byte[] data){
        String clazzName = readMethodName(data, cursor);
        if(imports != null){
            clazzName = imports.findFull(clazzName);
        }

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
                Proxy proxy = proxy(inst);
                fillMap(inst, proxy, data, ++cursor);
            } catch (Exception e) {
                if(e instanceof TsonSyntaxException){
                    throw (TsonSyntaxException)e;
                }
                throw TsonSyntaxException.make(lastCharName, data, e);
            }
        }
        return inst;
    }


    private Proxy proxy(Object obj){
        Proxy proxy = this.proxy;
        if(proxy != null){
            proxy.init(obj);
            return proxy;
        }
        return this.proxy = new Proxy(obj);
    }


    private TsonObj processCalls(Object inst, final byte[] data){
        int lastCharName;
        while(data[cursor] == '.'){
            lastCharName = cursor-1;
            ++this.cursor;
            try {
                AtomicBoolean isVoid = new AtomicBoolean(false);
                if(inst instanceof TsonObj){
                    inst = ((TsonObj)inst).getField();
                }
                TsonObj res = invokeCustom(inst, data, isVoid);
                if(!isVoid.get())inst = res.getField();

                byte cur = data[cursor];
                while (cur == ' ' || cur == '\n') {
                    cur = data[++cursor];
                }
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
        private Class<?> clazz;
        private Object inst;

        private Proxy(Object inst) {
            this.inst = inst;
            this.clazz = inst.getClass();
        }

        private void init(Object inst){
            if(this.inst != inst) {
                this.inst = inst;
                this.clazz = inst.getClass();
            }
        }

        @Override
        public int size() {return 0;}
        @Override
        public boolean isEmpty() {return false;}
        @Override
        public boolean containsKey(Object key) {return false;}
        @Override
        public boolean containsValue(Object value) {return false;}
        @Override
        public TsonObj get(Object key) {return null;}
        @Override
        public Set<String> keySet() {return null;}
        @Override
        public Collection<TsonObj> values() {return null;}
        @Override
        public Set<Entry<String, TsonObj>> entrySet() {return null;}

        @Override
        public void putAll(Map<? extends String, ? extends TsonObj> m) {
            for (Map.Entry<? extends String, ? extends TsonObj> entry : m.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
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
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }


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
        if(super.getLength() == 0)return "";
        return cString();
    }


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


    private static boolean isFunc(final byte[] data, final int cursor){
        final byte cur = data[cursor];
        if(cur == 'f' || cur == 'F')
            return data[cursor + 1] == 'u' && data[cursor + 2] == 'n' && data[cursor + 3] == 'c';
        return false;
    }


    private static boolean isNew(final byte[] data, final int cursor){
        return data[cursor] == 'n' && data[cursor + 1] == 'e' && data[cursor + 2] == 'w' && data[cursor + 3] == ' ';
    }


    private static boolean isThis(final byte[] data, final int cursor){
        return data[cursor] == 't' && data[cursor + 1] == 'h' && data[cursor + 2] == 'i' && data[cursor + 3] == 's';
    }


    private static boolean isImport(final byte[] data, final int cursor){
        return  data[cursor    ] == 'i' &&
                data[cursor + 1] == 'm' &&
                data[cursor + 2] == 'p' &&
                data[cursor + 3] == 'o' &&
                data[cursor + 4] == 'r' &&
                data[cursor + 5] == 't' &&
                data[cursor + 6] == ' ';
    }


    private static boolean isEngine(final byte[] data, final int cursor){
        return  data[cursor] == 'e' &&
                data[cursor + 1] == 'n' &&
                data[cursor + 2] == 'g' &&
                data[cursor + 3] == 'i' &&
                data[cursor + 4] == 'n' &&
                data[cursor + 5] == 'e' &&
                data[cursor + 6] == ' ';
    }


    private TsonStr getTsonStr(final byte[] data, final byte end){
        return new TsonStr(getStr(data, cursor, end));
    }


    private String getStr(final byte[] data, final int cursor, final byte end){
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
        this.cursor = cur+1;
        return cString();
    }


    private String getKey(final byte[] data, int cursor){
        byte c = data[cursor];
        if(c == '"' || c == '\'') {
            int prev = this.cursor;
            String result = getStr(data, cursor, c);
            cursor = this.cursor;

            for(final int length = data.length; cursor < length; ++cursor){
                if((c = data[cursor]) == '=') break;
                if(c == ',' || c == '}'){
                    this.cursor = prev;
                    return null;
                }
            }
            this.cursor = cursor;
            return result;
        }
        clear();
        for(final int length = data.length; cursor < length; ++cursor){
            if((c = data[cursor]) == '=') break;
            if(c == ',' || c == '}'){
                return null;
            }
            if(c == ' ' || c == '\n')continue;
            append(c);
        }
        this.cursor = cursor;
        return cString();
    }


    private void skipAb(byte[] data, int cursor){
        while (cursor <= data.length) {
            if(data[cursor] == ')')break;
            ++cursor;
        }
        this.cursor = cursor;
    }


    private TsonObj readClassOrBool(byte[] data, int cursor){
        if(isTrue(data, cursor)){
            skipAb(data, cursor + 3);
            return TsonBool.TRUE;
        }
        if(isFalse(data, cursor)){
            skipAb(data, cursor + 4);
            return TsonBool.FALSE;
        }
        int cur = cursor;
        clear();
        for(byte c; cur <= data.length; ++cur){
            if((c = data[cur]) == ')')break;
            append(c);
        }
        this.cursor = cur;
        try {
            return new TsonClass(manager, cString());
        } catch (IllegalArgumentException e){
            throw TsonSyntaxException.make(cur, data, e.getMessage());
        }
    }
}