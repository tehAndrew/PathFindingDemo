public class Pair {
    int x;
    int y;

    Pair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Pair))
            return false;

        Pair p = (Pair) o;

        return x == p.x && y == p.y;
    }
}