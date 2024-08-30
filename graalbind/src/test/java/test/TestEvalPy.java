package test;

import org.graalvm.polyglot.*;
import plus.tson.TsonMap;

import java.util.function.Consumer;


public class TestEvalPy {


    public static void main(String[] args) {


        Context ctx = Context.newBuilder().option("engine.WarnInterpreterOnly", "false")
                .allowAllAccess(true).build();

        ctx.eval("python", """
                from polyglot import export_value as export
                import java
                
                import java.util.ArrayList
                
                al = ArrayList()
                
                @export
                def func(name = None):
                    name = name + "test";
                    print(name)

                    def tfc(arg):
                        print("TFK")

                    java.type("test.TestEvalPy").func1A(tfc)


                """);

        Value func = ctx.getPolyglotBindings().getMember("func");
        func.execute("AAA");

        //System.out.println(vl.execute("AAA"));
    }

    public static void func1A(Consumer<Object> src){
        src.accept("AA");
    }
}
