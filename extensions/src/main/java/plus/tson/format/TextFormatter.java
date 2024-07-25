package plus.tson.format;

import plus.tson.utl.Tuple;
import java.util.ArrayList;


public class TextFormatter<T> implements IVarGetter<T>{
    protected final String[] items;
    protected final IVarGetter<T>[] getters;

    protected TextFormatter(Tuple<String[], IVarGetter<T>[]> data){
        this.items = data.A;
        this.getters = data.B;
    }


    protected TextFormatter(String[] items, IVarGetter<T>[] getters){
        this.items = items;
        this.getters = getters;
    }


    public static<T> TextFormatter<T> compile(String format, IVarProvider<T> provider){
        return new TextFormatter<>(compileFormat(format, provider));
    }


    protected static<T> Tuple<String[], IVarGetter<T>[]> compileFormat(String format, IVarProvider<T> provider){
        return compileFormat(format, provider,"%","%");
    }


    private static boolean isAct(char[] src, int cursor, char[] check){
        //if(cursor > 0 && src[cursor-1] == '\\')return false;
        int lim = Math.min(cursor+check.length, src.length);
        int n = 0;
        for (int i=cursor;i<lim;i++){
            if(src[i]!=check[n])return false;
            n++;
        }
        return true;
    }


    public static<T> Tuple<ArrayList<String>,ArrayList<String>> prepare(String format, String st, String ed){
        ArrayList<String> items = new ArrayList<>();
        ArrayList<String> getters00 = new ArrayList<>();
        char[] stc = format.toCharArray();
        boolean text = true;
        int sCursor = 0;
        boolean flag = false;

        char[] stt0 = st.toCharArray();
        char[] end0 = ed.toCharArray();

        for(int i=0;i<stc.length;i++){
            if(text){
                if(isAct(stc, i, stt0)){
                    if(flag){
                        items.add(items.remove(items.size()-1)+format.substring(sCursor, i));
                        flag = false;
                    } else {
                        items.add(format.substring(sCursor, i));
                    }
                    text = false;
                    sCursor = i+stt0.length;
                }
            } else {
                if(isAct(stc, i, end0)){
                    text = true;
                    String fmGetterName = format.substring(sCursor, i).trim();
                    getters00.add(fmGetterName);

                    sCursor = i+stt0.length;
                }
            }
        }

        if(text){
            items.add(format.substring(sCursor));
        } else {
            throw new RuntimeException("Syntax error!");
        }
        return new Tuple<>(items, getters00);
    }


    public static<T> Tuple<String[], IVarGetter<T>[]> compileFormat(String format, IVarProvider<T> provider, String st, String ed){
        Tuple<ArrayList<String>, ArrayList<String>> res = prepare(format, st, ed);
        return buildFormat(provider, res.A, res.B);
    }


    public static<T> Tuple<String[], IVarGetter<T>[]> buildFormat(IVarProvider<T> provider, ArrayList<String> items, ArrayList<String> vars){
        provider = new ProxyProvider<>(provider);
        ArrayList<IVarGetter<T>> getters0 = new ArrayList<>();
        int cursor = 0;
        for (String fmGetterName:vars) {
            IVarGetter<T> getter = provider.getFmGetter(fmGetterName);
            boolean isConst = true;
            if (getter == null) {
                System.out.println("VarGetter [" + fmGetterName + "] not valid. Ignored.");
                getter = new ConstVarGetter<>(fmGetterName);
            } else {
                isConst = IVarGetter.isConst(getter);
            }
            if (isConst) {
                items.add(cursor,
                        items.remove(cursor) + getter.get(null) + items.remove(cursor)
                );
            } else {
                getters0.add(getter);
                ++cursor;
            }
        }

        IVarGetter<T>[] getters = getters0.toArray(new IVarGetter[0]);
        int sCursor = 3000;
        for(int i=0;i<getters.length;i++){
            for(int j=i+1;j<getters.length;j++){
                IVarGetter<T> g1 = getters[i];
                if(g1.equals(getters[j])){
                    getters[j] = g1;
                }
            }
            sCursor-=(getters.length-i);
            if(sCursor<0)break;
        }
        return new Tuple<>(items.toArray(new String[0]), getters);
    }


    public boolean maybeSimplify(){
        return isConst() || getters.length==1 && (items[0].isEmpty() && items[1].isEmpty());
    }


    public IVarGetter<T> simplify(){
        if(!maybeSimplify())return this;
        if(isConst())return new ConstVarGetter<>(items[0]);
        return getters[0];
    }


    public boolean isConst(){
        return getters.length == 0;
    }


    public String format(T t){
        return get(t);
    }


    @Override
    public String get(T t){
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<getters.length;i++){
            String result = getters[i].get(t);
            if(result==null){
                builder.append(items[i]);
            } else {
                builder.append(items[i]).append(getters[i].get(t));
            }
        }
        return builder.append(items[items.length-1]).toString();
    }


    public String format(String... args){
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<args.length;i++){
            builder.append(items[i]).append(args[i]);
        }
        return builder.append(items[items.length-1]).toString();
    }


    public TextFormatter<T> replace(String from, String to){
        String[] newItems = new String[items.length];
        for(int i=0;i<newItems.length;i++){
            newItems[i] = items[i].replace(from, to);
        }
        return new TextFormatter<>(newItems, getters);
    }


    public int getCountArgs(){
        return getters.length;
    }
}
