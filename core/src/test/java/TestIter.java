import plus.tson.*;
import plus.tson.security.ClassManager;
import plus.tson.utl.Te4HashMap;

import java.lang.reflect.InvocationTargetException;

public class TestIter {
    public static void main(String[] args) {
        TsonMap map = new TsonMap("""
                {k=(1), k2=(2), k3=(3), k4=(4), k5=(5), k6=(6), k7=(7), k8=(8)}
                """);

        System.out.println(new TJsonParser("{key: 10}", true).getMap());
        System.out.println(new TJsonParser("{\"key\": 10}", false /*по умолчанию*/).getMap());

        new ClassManager(){
            @Override
            public Object newInstance(Class<?> clazz, Object... args)
                    throws InvocationTargetException, NoSuchMethodException,
                    InstantiationException, IllegalAccessException {
                if(clazz == Example.class){
                    return new Example((String) args[0], (String) args[1]);
                }
                return TsonClass.createInst(clazz, args);
            }
        };

//        Te4HashMap<String, TsonObj>.FastEntryIter it = map.fastIter();
//
//        while (it.hasNext()) {
//            it.next();
//            System.out.println(it.getKey()+">"+it.getValue());
//
//        }
    }


    static class Example implements TsonSerelizable {
        private final String k;
        private final String v;
        public Example(String k, String v){
            this.k = k;this.v = v;
        }

        public Example(TsonMap mp){
            k = mp.getStr("k");
            v = mp.getStr("v");
        }

        @Override
        public TsonObj toTson() {
            TsonMap mp = new TsonMap();
            mp.put("k", k);
            mp.put("v",v);
            return mp;
        }
    }
}
