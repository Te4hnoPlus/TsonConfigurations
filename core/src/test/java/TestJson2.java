import plus.tson.*;


public class TestJson2 {

    public static void main(String[] args) {
        //long l = System.currentTimeMillis();
        TsonMap map = null;{
            map = new TJsonParserV2(
                    """
{
    "PLANETS": {
        "PIRACY":[{
            "normal": [{"min": 1,"biomes": [111,112,113],"max":3}],
            "def"   : {"bounds":[
                [[[114,115]],[115,117]],
                [[118,119],[110,111]],[[112,113]]
            ]}
        }],
        "IMPERIAL":[{
          "normal": [{"min": 1,"biomes": [114,115,116],"max":4}],
          "def"   : {"bounds":[
            [[[117,118]],[119,120]],
            [[223,6222],[224,225]],[[223,5222]]
          ]}
        }],
        "VOIDS":[{
          "normal": [{"min": 1,"biomes": [224,221,223],"max":3}],
          "def"   : {"bounds":[
            [[[223,7222]],[224,225]],
            [[223,4222],[224,225]],[[223,1222]]
          ]}
        }]
    },
    "MOONS": {
      "PIRACY":[{
        "def"   : {"bounds":[
          [[[423,3222]],[224,225]],
          [[2123,2212],[2274,225]],[[2213,2242]]
        ]}
      }],
      "VOIDS":[{
        "normal": [{"min": 1,"biomes": [224,221,223],"max":3}],
        "def"   : {"bounds":[
          [[[223,7222]],[224,225]],
          [[223,9222],[224,225]],[[223,2222]]
        ]}
      }]
    }
}
                            """
            ).getMap();}


        System.out.println(new TJsonWriter(map, 4, 25, true));


        //TsonFile.write(new File("test.txt"), "AGAGAG".getBytes());
//        TsonMap map = new TsonMap();
//        map.put("k1", "v1");
//        map.put("k2", "v2");
//        map.put("k3", l);
//
//        System.out.println(l);
//
//        System.out.println(map.toJsonStr());

//        System.out.println(new TsonMap("{k=true,v=false}"));
    }
}
