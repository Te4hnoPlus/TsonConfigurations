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
        return compileFormat(format, provider,'%','%');
    }


    protected static<T> Tuple<String[], IVarGetter<T>[]> compileFormat(String format, IVarProvider<T> provider,char st,char ed){
        provider = new ProxyProvider<>(provider);
        ArrayList<String> items = new ArrayList<>();
        ArrayList<IVarGetter<T>> getters0 = new ArrayList<>();
        char[] stc = format.toCharArray();
        boolean text = true;
        int sCursor = 0;
        boolean flag = false;

        for(int i=0;i<stc.length;i++){
            char cur = stc[i];
            if(text){
                if(cur==st && (i==0 || stc[i-1] != '\\')){
                    if(flag){
                        items.add(items.remove(items.size()-1)+format.substring(sCursor, i));
                        flag = false;
                    } else {
                        items.add(format.substring(sCursor, i));
                    }
                    text = false;
                    sCursor = i+1;
                }
            } else {
                if(cur==ed && i>1 && stc[i-1] != '\\'){
                    text = true;
                    IVarGetter<T> getter = provider.get(format.substring(sCursor, i).trim());
                    if(IVarGetter.isConst(getter)){
                        items.add(items.remove(items.size()-1)+getter.get(null));
                        flag = true;
                    } else {
                        getters0.add(getter);
                    }
                    sCursor = i+1;
                }
            }
        }
        if(text){
            if(flag){
                items.add(items.remove(items.size()-1)+format.substring(sCursor));
            } else {
                items.add(format.substring(sCursor));
            }
        } else {
            throw new RuntimeException("Syntax error!");
        }

        IVarGetter<T>[] getters = getters0.toArray(new IVarGetter[0]);
        sCursor = 3000;
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
        return getters.length==0;
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
class ProxyProvider<T> implements IVarProvider<T>{
    private final IVarProvider<T> parent;
    ProxyProvider(IVarProvider<T> parent) {
        this.parent = parent;
    }


    @Override
    public IVarGetter<T> get(String name){
        name = name.trim();
        boolean flag = false;
        int split = -1;
        boolean nextIgnore = false;

        char[] chars = name.toCharArray();

        for(int i=0;i<chars.length;i++){
            char c = chars[i];
            if(nextIgnore){
                nextIgnore = false;
                continue;
            } else {
                if(c=='\\'){
                    nextIgnore = true;
                    continue;
                }
            }
            if(flag){
                if(c=='"')flag = false;
                continue;
            } else {
                if(c=='"'){
                    flag = true;
                    continue;
                }
            }
            if(c=='/'){
                split = i;
            }
        }

        if(split>0){
            return new VarDefGetter<>(
                    get0(name.substring(0, split)),
                    get(name.substring(split+1))
            );
        }
        return get0(name);
    }


    private IVarGetter<T> get0(String name) {
        name = name.trim().replace("\\","");
        if(name.charAt(0)=='"' && name.charAt(name.length()-1)=='"'){
            return new ConstVarGetter<>(name.substring(1, name.length()-1));
        }
        return parent.get(name);
    }
}