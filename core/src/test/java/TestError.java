import plus.tson.TsonMap;
import plus.tson.exception.TsonSyntaxException;

public class TestError {
    public static void main(String[] args) {
        String invalidJ = """
                {
                PLANETS={
                    PIRACY=[{
                        normal=[{min=(1),biomes=[(224),(221),(223)],max=(3)}],
                        def={bounds=[[[(223),(222)]]],[(224),(225)]],[[(223),(222)],[(224),(225)]],[[(223),(222)]]]}
                    }],
                    IMPERIAL=[{
                        normal=[{min=(1),biomes=[(224),(221),(223)],max=(3)}],
                        def={bounds=[[[(223),(222)]]],[(224),(225)]],[[(223),(222)],[(224),(225)]],[[(223),(222)]]]}
                    }],
                    NEUTRAL=[{
                        normal=[{min=(1),biomes=[(224),(221),(223)],max=(3)}],
                        def={bounds=[[[(223),(222)]]],[(224),(225)]],[[(223),(222)],[(224),(225)]],[[(223),(222)]]]}
                    }],
                    VOIDS=[{
                        normal=[{min=(1),biomes=[(224),(221),(223)],max=(3)}],
                        def={bounds=[[[(223),(222)]]],[(224),(225)]],[[(223),(222)],[(224),(225)]],[[(223),(222)]]]}
                    }]
                }
                MOONS={
                    PIRACY=[{
                        normal=[{min=(1),biomes=[(224),(221),(223)],max=(3)}],
                        def={bounds=[[[(223),(222)]]],[(224),(225)]],[[(223),(222)],[(224),(225)]],[[(223),(222)]]]}
                    }],
                    IMPERIAL=[{
                        normal=[{min=(1),biomes=[(224),(221),(223)],max=(3)}],
                        def={bounds=[[[(223),(222)]]],[(224),(225)]],[[(223),(222)],[(224),(225)]],[[(223),(222)]]]}
                    }],
                    NEUTRAL=[{
                        normal=[{min=(1),biomes=[(224),(221),(223)],max=(3)}],
                        def={bounds=[[[(223),(222)]]],[(224),(225)]],[[(223),(222)],[(224),(225)]],[[(223),(222)]]]}
                    }],
                    VOIDS=[{
                        normal=[{min=(1),biomes=[(224),(221),(223)],max=(3)}],
                        def={bounds=[[[(223),(222)]]],[(224),(225)]],[[(223),(222)],[(224),(225)]],[[(223),(222)]]]}
                    }]
                }
                                
                }
                """;

        try {
            TsonMap map = new TsonMap(invalidJ);
        } catch (TsonSyntaxException e){
            System.err.println(e.getMessage());
        }
    }
}
