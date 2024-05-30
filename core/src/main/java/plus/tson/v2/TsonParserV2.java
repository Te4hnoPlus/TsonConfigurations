package plus.tson.v2;


import plus.tson.exception.TsonSyntaxException;
import plus.tson.utl.CharStrBuilder;
import plus.tson.utl.TupleMutable;


public class TsonParserV2 {
    private static final char[] THIS = "this.".toCharArray();
    private static final char[] NEW  = "new".toCharArray();

    // Object{
    //  data1 = "value1",
    //  data2 = "value2",
    //  new('var1', 'var2'),   //move to up
    //  data3 = this.call('data'),
    //  data4 = callStatick(),
    //  callStatickNoReturn()
    // }

    private final char[] chars;
    private int cursor = 0;
    private int preCursor = 0;
    private final StringBuilder builder = new StringBuilder();

    public TsonParserV2(String data) {
        chars = data.toCharArray();
    }



    private Func readAction(Object inst){
        skipToNext();
        switch (cur()){
            case '{':{

            }
            case '[':{

            }
            case '"':{

            }
            case '\'':{

            }
            default:{
                if(startWith(THIS)){
                    skip(THIS);
                    //
                } else if(startWith(NEW)){
                    skip(NEW);
                    //
                } else {
                    //
                }
            }
        }
        return null;
    }


    private char cur(){
        return chars[cursor];
    }


    private void radMethodName(TupleMutable<String, Type> result){
        StringBuilder b = builder;
        b.setLength(0);
        result.B = Type.STRING;

        int cursor = this.cursor;
        char[] itr = TsonParserV2.this.chars;

        while (cursor < itr.length) {
            char chr = itr[cursor];
            if(isEmpty(chr)) continue;
            if(chr == '(') {
                result.B = Type.METHOD;
                break;
            } else if (chr == '[') {
                result.B = Type.LIST;
                break;
            } else if (chr == '{') {
                result.B = Type.MAP;
                break;
            } else if(chr == ','){
                break;
            } else
                if(chr == ')' || chr == ']' || chr == '}') throw new RuntimeException("Syntax error at [" + cursor+"]");

            b.append(chr);
            cursor++;
        }
        if(b.lastIndexOf(" ") != -1) throw new RuntimeException("Syntax error at [" + cursor+"], method name should not be space");
        result.A = b.toString();
    }


    enum Type{
        STRING, MAP, METHOD, LIST
    }


    public static boolean isNum(char chr){
        return chr >= '0' && chr <= '9';
    }


    public static boolean isEmpty(char chr){
        return chr == ' ' || chr == '\t' || chr == '\n' || chr == '\r';
    }


    private void skip(char... chars){
        this.cursor += chars.length;
    }


    private void skip(int num){
        this.cursor += num;
    }


    private boolean startWith(char... chars){
        int cursor = this.cursor;
        char[] itr = TsonParserV2.this.chars;

        for(char chr : chars){
            if(cursor >= itr.length) return false;
            if(chr != itr[cursor]) return false;
            cursor++;
        }
        return true;
    }


    private void skipToNext(){
        int cursor = this.cursor;
        char[] itr = TsonParserV2.this.chars;
        while (cursor < itr.length) {
            if(!isEmpty(itr[cursor])) break;
            cursor++;
        }
        this.cursor = cursor;
    }


    private interface Func{
        Object apply(Object[] args) throws Exception;
    }


    public static class Constructor implements Func {
        private final Class<?> clazz;

        public Constructor(Class<?> clazz, Class<?>... types) {
            this.clazz = clazz;
        }

        @Override
        public Object apply(Object[] objects) throws Exception {
            return clazz.getDeclaredConstructor(TsonParserV2.types(objects)).newInstance(objects);
        }
    }


    public static class Mtd implements Func {
        final Class<?> clazz;
        final String name;
        Object inst;

        public Mtd(Object inst, String name) {
            this.clazz = inst.getClass();
            this.inst = inst;
            this.name = name;
        }


        public Mtd(Class<?> clazz, String name) {
            this.clazz = clazz;
            this.name = name;
        }


        @Override
        public Object apply(Object[] objects) throws Exception {
            return clazz.getDeclaredMethod(name, TsonParserV2.types(objects)).invoke(inst, objects);
        }
    }


    public static class FieldAcc extends Mtd{
        public FieldAcc(Object inst, String name) {
            super(inst, name);
        }

        public FieldAcc(Class<?> clazz, String name) {
            super(clazz, name);
        }

        @Override
        public Object apply(Object[] objects) throws Exception {
            return clazz.getDeclaredField(name).get(inst);
        }
    }


    public static class ConstAcc implements Func{
        private final Object value;

        public ConstAcc(Object value) {
            this.value = value;
        }

        @Override
        public Object apply(Object[] args) throws Exception {
            return value;
        }
    }


    private static Class<?>[] types(Object[] args){
        Class<?>[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            classes[i] = args[i].getClass();
        }
        return classes;
    }
}