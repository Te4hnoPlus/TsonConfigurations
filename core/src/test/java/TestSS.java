
import plus.tson.TsonMap;
import plus.tson.security.ClassManager;

public class TestSS {

    public static void main(String[] args) {
        TsonMap map = new TsonMap(new ClassManager.Empty(), """
{
    structures=[
        <(normal), {
            id=(1),
            chance=(0.5),
            biomes=["plains"],
            spawn-at=["grass"],
            structure="test",
            max-pow=(50),
            pos=[(0),(1),(0)],
            add=[(0)],
            settings=[
                ["rot-90"],
                ["struct-block"],
                ["entities", "m-left", "m-front", "ignore:air"]
            ]
        }>
    ],

    classes=[(normal), (sub-simple), (w), (w+), (multi), (sub-multi), (choose)]
}
                """);

        System.out.println(map);

        //TsonFile file = new TsonFile("solar-repo.tson");
        //System.out.println(file);

        //TsonList list = new TsonList("[[['1'],'2'],'3']");
        //System.out.println(list);
    }



}
