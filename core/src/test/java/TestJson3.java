import plus.tson.TJsonParser;
import plus.tson.TsonMap;

public class TestJson3 {
    public static void main(String[] args) {

        TsonMap map = new TJsonParser(
"{\"root1\": [1, [2, 2, [3, 3, 3]]], \"root2\": [1, [2, 2, [3, 3, 3]]]}").getMap();

        System.out.println(map);

        TsonMap map2 = new TJsonParser(map.toJsonObj()).getMap();

        System.out.println(map2);
    }
}
