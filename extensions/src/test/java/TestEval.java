import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public class TestEval {
    public static int num1 = 10;

    private static interface Func{
        void accept(String str);
    }

    private static void preLoad(Class<?> clazz) {
        try {
            Class.forName(clazz.getName());
        } catch (Exception e){e.printStackTrace();}
    }

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        GroovyShell shell = new GroovyShell(TestEval.class.getClassLoader());

        Script script = shell.parse("""
import java.util.function.Consumer

class Test implements Consumer<String>{
    void accept(String str){
        println "AAA" + str
    }
}
return new Test()
        """);



        Consumer<String> test = (Consumer<String>)script.run();
        //test.accept("AAA");
//        FuncCompiler.Func1A<Object> func0 = FuncCompiler.compile(test.getClass(), "accept", String.class);
//        func0.call(test, "AAA");
//
//        TsonMap map = new TsonMap("{A={}}");
//
//        FuncCompiler.Func1A<Object> func = FuncCompiler.compile(map.getClass(), "getMap", String.class);
//        System.out.println(func.call(map, "A")); //map.getMap("A");


    }
}