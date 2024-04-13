package plus.tson.format;


public class ProxyProvider<T> implements IVarProvider<T> {
    private final IVarProvider<T> parent;

    ProxyProvider(IVarProvider<T> parent) {
        this.parent = parent;
    }


    @Override
    public IVarGetter<T> getFmGetter(String name) {
        name = name.trim();
        boolean flag = false;
        int split = -1;
        boolean nextIgnore = false;

        char[] chars = name.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (nextIgnore) {
                nextIgnore = false;
                continue;
            } else {
                if (c == '\\') {
                    nextIgnore = true;
                    continue;
                }
            }
            if (flag) {
                if (c == '"') flag = false;
                continue;
            } else {
                if (c == '"') {
                    flag = true;
                    continue;
                }
            }
            if (c == '/') {
                split = i;
            }
        }

        if (split > 0) {
            return new VarDefGetter<>(
                    get1(name.substring(0, split)),
                    getFmGetter(name.substring(split + 1))
            );
        }
        return get1(name);
    }


    private IVarGetter<T> get1(String name) {
        IVarGetter<T> result = get0(name);
        if (result == null) {
            System.out.println("VarGetter [" + name + "] not valid. Ignored.");
            return new ConstVarGetter<>(name);
        }
        return result;
    }


    private IVarGetter<T> get0(String name) {
        name = name.trim().replace("\\", "");
        if (name.charAt(0) == '"' && name.charAt(name.length() - 1) == '"') {
            return new ConstVarGetter<>(name.substring(1, name.length() - 1));
        }
        return parent.getFmGetter(name);
    }
}
