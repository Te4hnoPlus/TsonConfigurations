import plus.tson.TJsonParserV2;
import plus.tson.TsonMap;

public class TestJson3 {
    public static void main(String[] args) {

        TsonMap map = new TJsonParserV2(
"""
{"root1": [1, [2, 2, [3, 3, 3]]], "root2": [1, [2, 2, [3, 3, 3]]]}
""").getMap();

        System.out.println(map);

        TsonMap map2 = new TJsonParserV2(map.toJsonObj()).getMap();

        System.out.println(map2);
    }
}
