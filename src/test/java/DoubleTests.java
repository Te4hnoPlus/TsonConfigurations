import java.util.Objects;

public class DoubleTests {
    public static void main(String[] args) {
        System.out.println(Integer.MAX_VALUE>>5);

    }


    private static class BoxKey{
        private final int minX, maxX, minY, maxY, minZ, maxZ;

        public BoxKey(double _minX, double _minY, double _minZ, double _maxX, double _maxY, double _maxZ){
            minX = ((int)_minX*100)>>5;
            maxX = ((int)_maxX*100)>>5;

            minY = ((int)_minY*100)>>5;
            maxY = ((int)_maxY*100)>>5;

            minZ = ((int)_minZ*100)>>5;
            maxZ = ((int)_maxZ*100)>>5;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o.getClass() != BoxKey.class) return false;
            BoxKey boxKey = (BoxKey) o;
            return minX == boxKey.minX && maxX == boxKey.maxX && minY == boxKey.minY && maxY == boxKey.maxY && minZ == boxKey.minZ && maxZ == boxKey.maxZ;
        }


        @Override
        public int hashCode() {
            return 31 * (31 * (31 * (31 * (31 * minX + maxX) + minY) + maxY) + minZ) + maxZ;
        }
    }
}
