import plus.tson.STsonParser;

import plus.tson.TsonMap;
import plus.tson.TsonFunc;


public class TestST2 {

    public static void main(String[] args) {
        STsonParser parser = new STsonParser("""
import plus.tson.TsonBool
import plus.tson.TsonClass

{b = new TsonBool(true), f = func(arg1, arg2){return arg1 + arg2}, d = 10, e = TsonClass::types}

                """).readImports().with(TsonFunc.COMPILER);

        TsonMap map = parser.getMap();
        TsonFunc func = map.getCustom("f");

        System.out.println(func.call("A1", "-A2"));


        System.out.println(map);

    }
}
