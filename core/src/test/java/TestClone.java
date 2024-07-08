import plus.tson.TsonMap;

public class TestClone {
    public static void main(String[] args) {
        TsonMap map = new TsonMap("{k1='v1', k2=['v2', 'v3'], k3='v4'}");
        TsonMap clone = map.clone();
        System.out.println(map);
        System.out.println(clone);
    }
}
