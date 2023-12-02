import plus.tson.utl.Te4HashSet;


public class TestSet {
    public static void main(String[] args) {
        Te4HashSet<String> set = new Te4HashSet<>();


        System.out.println(set.add("tets"));
        System.out.println(set.add("tets"));

        set.add("tets2");

        System.out.println(set.remove("tets"));
        System.out.println(set.remove("tets"));
        System.out.println(set.remove("tets"));

        System.out.println(set);

    }
}